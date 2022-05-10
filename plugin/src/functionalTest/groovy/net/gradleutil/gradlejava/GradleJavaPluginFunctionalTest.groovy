package net.gradleutil.gradlejava

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir


class GradleJavaPluginFunctionalTest extends Specification {
    @TempDir
    private File projectDir

    private getBuildFile() {
        new File(projectDir, "build.gradle")
    }

    private getSettingsFile() {
        new File(projectDir, "settings.gradle")
    }

    private String runProc(String command) {
        println "running command ${command}"
        def sb = new StringBuilder()
        def proc = ['/bin/sh', '-c', command].execute()
        proc.in.eachLine { line -> println line; sb.append(line) }
        proc.err.eachLine { line -> println line }
        proc.out.close()
        proc.waitForOrKill(120 * 1000)
        return sb.toString()
    }

    def "can run task"() {
        given:
        settingsFile << ""
        buildFile << """
        plugins {
            id('net.gradleutil.gradle-java')
        }
        """

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("wrapper")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains(":wrapper")
        println "RUNNIN"
        runProc("${projectDir.absolutePath}/gradlew --version -i").contains('Build time:')
    }
}
