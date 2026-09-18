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
    }

    post {
        always {
            sh 'docker rm -f devops-demo-jenkins-test || true'
        }

        success {
            echo 'CI PIPELINE SUCCESSFUL'
        }

        failure {
            echo 'CI PIPELINE FAILED'
        }
    }
}
