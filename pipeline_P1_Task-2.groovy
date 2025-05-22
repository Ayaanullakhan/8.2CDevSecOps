pipeline {
    agent any
    environment{
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
                script{
                    try{
                        sh 'npm test || tee test.log' 
                        currentBuild.description = 'Tests Passed'
                    } catch (err) {
                        currentBuild.result = 'FAILURE'
                        currentBuild.description = 'Tests Failed'
                        throw err
                    }
                }
            }
        post {
            always{
                emailext(
                    subject: "Run Tests - ${currentBuild.currentResult}",
                    body: """<p>The <b>Run Tests</b> stage has completed with status: <b>${currentBuild.currentResult}</b></p>""",
                    to: "${env.RECIPIENTS}",
                    attachmentsPattern: 'test.log'
                    )
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
                        currentBuild.description += " | Security Scan Passed"
                    } catch (err) {
                        currentBuild.result = 'FAILURE'
                        currentBuild.description += " | Security Scan Failed"
                        throw err
                    }
                }
            }
            post {
                always {
                    emailext (
                        subject: "Security Scan - ${currentBuild.currentResult}",
                        body: """<p>The <b>Security Scan</b> stage has completed with status: <b>${currentBuild.currentResult}</b></p>""",
                        to: "${env.RECIPIENTS}",
                        attachmentsPattern: 'audit.log'
                    )
                }
            }
        }
    }
}
