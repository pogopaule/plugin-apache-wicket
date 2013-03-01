package de.nerox.forge.wicket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.DependencyResolver;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class WicketTest extends AbstractShellTest {

	@Inject
	private DependencyResolver resolver;

	@Deployment
	public static JavaArchive getDeployment() {
		return AbstractShellTest.getDeployment().addPackages(true, Wicket.class.getPackage());
	}

	@Test
	public void wicket_setup_should_add_wicket_core_to_pom() throws Exception {
		Project project = setupProject();
		DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
		assertTrue(dependencyFacet.hasEffectiveDependency(DependencyBuilder.create("org.apache.wicket:wicket-core")));
	}

	@Test
	public void show_info_if_wicket_core_is_already_present() throws Exception {
		setupProject();
		getShell().execute("wicket setup");
		assertEquals("The Apache Wicket Core dependency is already in your pom.", getLastOutputLine());
	}

	private String getLastOutputLine() {
		String[] lines = getOutput().split("\r\n|\r|\n");
		return lines[lines.length - 1];
	}

	private Project setupProject() throws Exception {
		Project project = initializeProject(PackagingType.WAR);
		queueInputLines("0");
		getShell().execute("wicket setup");
		return project;
	}

}
