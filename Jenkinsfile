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

                    sleep 15

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
