package de.nerox.forge.wicket;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.DependencyResolver;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;

public class WicketTest extends AbstractShellTest {

	@Inject
	private DependencyResolver resolver;

	@Deployment
	public static JavaArchive getDeployment() {
		return AbstractShellTest.getDeployment().addPackages(true, Wicket.class.getPackage());
	}

	@Test
	public void setup_should_add_wicket_core_to_pom() throws Exception {
		Project project = setupProject();
		DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
		assertTrue(dependencyFacet.hasEffectiveDependency(DependencyBuilder.create("org.apache.wicket:wicket-core")));
	}

	@Test
	public void show_info_if_wicket_core_is_already_present() throws Exception {
		setupProject();
		getShell().execute("wicket setup");
		assertTrue(getOutput().contains("The Apache Wicket Core dependency is already in your pom."));
	}

	@Test
	public void setup_should_add_web_xml_to_project_without_web_xml() throws Exception {
		Project project = setupProject();
		WebResourceFacet webResourceFacet = project.getFacet(WebResourceFacet.class);
		FileResource<?> webXml = webResourceFacet.getWebResource("WEB-INF/web.xml");
		assertTrue(webXml.exists());
		assertWebXmlContainsTypicalStrings(project, webXml);
	}

	private void assertWebXmlContainsTypicalStrings(Project project, FileResource<?> webXml) throws IOException {
		String webXmlContent = IOUtils.toString(webXml.getResourceInputStream());
		assertTrue(webXmlContent
				.contains("http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"));
		String projectName = project.getProjectRoot().toString();
		assertTrue(webXmlContent.contains("display-name>" + projectName + "</display-name"));
		assertTrue(webXmlContent.contains("<filter-class>org.apache.wicket.protocol.http.WicketFilter</filter-class>"));
		assertTrue(webXmlContent.contains("<filter-name>wicket." + projectName + "</filter-name>"));
	}

	@Test
	public void setup_should_add_wicket_to_web_xml_in_project_with_existing_web_xml() throws Exception {
		Project project = initializeProject(PackagingType.WAR);
		WebResourceFacet webResourceFacet = project.getFacet(WebResourceFacet.class);
		FileResource<?> webXml = webResourceFacet.getWebResource("WEB-INF/web.xml");
		webXml.createNewFile();
		webXml.setContents("<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\" version=\"3.0\"></web-app>");
		executeWicketSetup();
		assertWebXmlContainsTypicalStrings(project, webXml);
	}

	@Test
	@Ignore
	public void configurationSwitch() {
		// TODO toggle between dev and prod config
		fail("implement");
	}

	@Test
	@Ignore
	public void setup_should_add_wicket_application_to_project() {
		fail("implement");
	}

	private Project setupProject() throws Exception {
		Project project = initializeProject(PackagingType.WAR);
		executeWicketSetup();
		return project;
	}

	private void executeWicketSetup() throws Exception {
		queueInputLines("0");
		getShell().execute("wicket setup");
	}

}
