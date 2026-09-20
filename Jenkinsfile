pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/kunax057/devops-cicd-project.git'
            }
        }

        stage('Build') {
            steps {
                sh 'cd app && mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(
                        credentialsId: 'sonarqube-token',
                        variable: 'SONAR_TOKEN'
                    )]) {
                        script {
                            def scannerHome = tool 'SonarScanner'

                            sh """
                                cd app
                                ${scannerHome}/bin/sonar-scanner \
                                  -Dsonar.host.url=http://localhost:9000 \
                                  -Dsonar.projectKey=devops-demo \
                                  -Dsonar.projectName='DevOps Demo' \
                                  -Dsonar.sources=src \
                                  -Dsonar.java.binaries=target/classes \
                                  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                  -Dsonar.token=\$SONAR_TOKEN
                            """
                        }
                    }
                }
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                withCredentials([string(
                    credentialsId: 'nvd-api-key',
                    variable: 'NVD_API_KEY'
                )]) {
                    sh '''
                        rm -rf dependency-check-report

                        /opt/dependency-check-13.0.0/bin/dependency-check.sh \
                          --project "DevOps Demo" \
                          --scan app \
                          --format HTML \
                          --out dependency-check-report \
                          --data /var/lib/jenkins/.dependency-check-data \
                          --nvdApiKey "$NVD_API_KEY" \
                          --nvdValidForHours 24

                        echo "OWASP Dependency-Check completed successfully"
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'cd app && docker build -t devops-demo:1.0 .'
            }
        }

        stage('Docker Test') {
            steps {
                sh '''
                    docker rm -f devops-demo-jenkins-test || true

                    docker run -d \
                      --name devops-demo-jenkins-test \
                      -p 8082:8081 \
                      devops-demo:1.0

                    echo "Waiting for application to become ready..."

                    for i in $(seq 1 30); do
                        if curl -sf http://localhost:8082/health > /dev/null; then
                            echo "Application is healthy"
                            curl -f http://localhost:8082/health
                            exit 0
                        fi

                        echo "Waiting... attempt $i/30"
                        sleep 2
                    done

                    echo "Application failed to become healthy"
                    docker logs devops-demo-jenkins-test
                    exit 1
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

                        docker tag devops-demo:1.0 $DOCKER_USERNAME/devops-demo:1.0

                        docker push $DOCKER_USERNAME/devops-demo:1.0

                        docker logout
                    '''
                }
            }
        }

        stage('Kubernetes Deploy') {
            steps {
                sh '''
                    kubectl apply -f k8s/deployment.yaml
                    kubectl apply -f k8s/service.yaml
                    kubectl rollout status deployment/devops-demo --timeout=120s
                '''
            }
        }

        stage('Ansible Configuration') {
            steps {
                sh '''
                    ansible-playbook \
                      -i ansible/inventory \
                      ansible/playbook.yml
                '''
            }
        }
    }

    post {
        always {
            sh 'docker rm -f devops-demo-jenkins-test || true'
        }

        success {
            echo 'CI/CD PIPELINE SUCCESSFUL'
        }

        failure {
            echo 'CI/CD PIPELINE FAILED'
        }
    }
}
