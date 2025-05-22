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
                    try {
                        sh 'npm test | tee test.log'
                    } catch (err) {
                        currentBuild.result = 'FAILURE'
                        throw err
                    }
                }
            }
            post {
                always {
                    script {
                        def status = currentBuild.result ?: 'SUCCESS'
                        def buildUrl = env.BUILD_URL
                        def body = """The Run Tests stage completed with status: ${status}.

Log file: ${buildUrl}artifact/test.log
Console output: ${buildUrl}console
"""
                        mail to: "${env.RECIPIENTS}",
                             subject: "Run Tests Stage - ${status}",
                             body: body
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
                    try {
                        sh 'npm audit | tee audit.log'
                    } catch (err) {
                        currentBuild.result = 'FAILURE'
                        throw err
                    }
                }
            }
            post {
                always {
                    script {
                        def status = currentBuild.result ?: 'SUCCESS'
                        def buildUrl = env.BUILD_URL
                        def body = """The Security Scan stage completed with status: ${status}.

Log file: ${buildUrl}artifact/audit.log
Console output: ${buildUrl}console
"""
                        mail to: "${env.RECIPIENTS}",
                             subject: "Security Scan Stage - ${status}",
                             body: body
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '*.log', fingerprint: true
        }
    }
}
