import com.cloudbees.groovy.cps.NonCPS

def findMaintainers(fileName) {
    if (fileExists(fileName)) {
        maintainersFile = readFile fileName
        maintainers = maintainersFile.tokenize("\n").collect { it.trim() }
        return maintainers
    }

    return []
}

def call(Closure body) {
    node {
        timestamps {
            wrap([$class: 'AnsiColorBuildWrapper']) {
                try {
                    body()
                } catch (e) {
                    maintainers = findMaintainers 'maintainers.txt'
                    for (int i = 0; i < maintainers.size(); i++) {
                        def address = maintainers.get(i)
                        mail subject: "${env.BUILD_TAG} failed", to: address, body: "Message: ${e.message}\nFailed build: ${env.BUILD_URL}"
                    }
                    throw e
                } finally {
                    deleteDir()
                }
            }
        }
    }
}
