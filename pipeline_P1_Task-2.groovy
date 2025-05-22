pipeline {
    agent any

    environment {
        RECIPIENTS = 'ayaanullakhan11@gmail.com'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Ayaanullakhan/8.2CDevSecOps.git'
            }
        }

        stage('Install Dependencies') {
            steps {
                sh 'npm install'
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    def status = 'SUCCESS'
                    try {
                        sh 'npm test | tee test.log'
                    } catch (err) {
                        status = 'FAILURE'
                        currentBuild.result = 'FAILURE'
                    } finally {
                        mail to: "${env.RECIPIENTS}",
                             subject: "Run Tests Stage - ${status}",
                             body: "The Run Tests stage completed with status: ${status}"
                    }
                }
            }
        }

        stage('Generate Coverage Report') {
            steps {
                sh 'npm run coverage || true'
            }
        }

        stage('NPM Audit (Security Scan)') {
            steps {
                script {
                    def status = 'SUCCESS'
                    try {
                        sh 'npm audit | tee audit.log'
                    } catch (err) {
                        status = 'FAILURE'
                        currentBuild.result = 'FAILURE'
                    } finally {
                        mail to: "${env.RECIPIENTS}",
                             subject: "Security Scan Stage - ${status}",
                             body: "The Security Scan stage completed with status: ${status}"
                    }
                }
            }
        }
    }
}
