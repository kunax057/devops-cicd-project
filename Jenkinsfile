pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/kunax057/devops-cicd-project.git'
            }
        }

        stage('Build') {
            steps {
                sh 'cd app && mvn clean package'
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
                                  -Dsonar.token=\$SONAR_TOKEN
                            """
                        }
                    }
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

                    for i in {1..30}; do
                        if curl -sf http://localhost:8082/health > /dev/null; then
                            echo "Application is healthy"
                            curl -f http://localhost:8082/health
                            break
                        fi

                        echo "Waiting... attempt $i/30"
                        sleep 2
                    done

                    curl -f http://localhost:8082/health
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
