package net.gradleutil.gradlejava


import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GradleJavaPluginTest extends Specification {
    def "plugin registers task"() {
        given:
        def rootProject = ProjectBuilder.builder().withName('root').build()
        rootProject.getTasksByName("tasks", false) // evaluate the project
        def project = ProjectBuilder.builder().withParent(rootProject).build()

        when:
        project.plugins.apply("net.gradleutil.gradle-java")
        project.getTasksByName("tasks", false) // evaluate the project

        then:
        project.rootProject.tasks.findByName("wrapper") != null
        def ext = project.extensions.getByType(GradleJavaPlugin.GradleJavaExtension)
        ext.downloadDir.get().asFile.name == 'tmp'
    }

}
