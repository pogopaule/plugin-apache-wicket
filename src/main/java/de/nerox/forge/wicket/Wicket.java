package de.nerox.forge.wicket;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.SetupCommand;

@Alias("wicket")
@Help("A plugin for Apache Wicket")
public class Wicket implements Plugin {

	private static final String WICKET_GROUP_ID = "org.apache.wicket";

	private static final String WICKET_VERSION_PROPERTY_NAME = "wicket.version";

	@Inject
	private ShellPrompt prompt;

	@Inject
	Project project;

	private DependencyFacet dependencyFacet;

	@SetupCommand(help = "sets up Apache Wicket for your project")
	public void setup(PipeOut out) {
		dependencyFacet = project.getFacet(DependencyFacet.class);

		installWicketCoreDependency(out);
		// setupWebXml();
	}

	private void setupWebXml() {
		FileResource<?> webXmlResource = getWebXml();
		Node webXml = XMLParser.parse(webXmlResource.getResourceInputStream());

		Node blablub = webXml.getOrCreate("blablub");

		webXmlResource.setContents(XMLParser.toXMLString(blablub));
	}

	private FileResource<?> getWebXml() {
		WebResourceFacet webResourceFacet = project.getFacet(WebResourceFacet.class);
		FileResource<?> webXml = webResourceFacet.getWebResource("WEB-INF/web.xml");
		if (!webXml.exists()) {
			webXml.createNewFile();
			// TODO web.xml content
		}
		return webXml;
	}

	private void installWicketCoreDependency(PipeOut out) {
		DependencyBuilder wicketCoreDependency = createWicketCoreDependency();
		if (!dependencyFacet.hasEffectiveDependency(wicketCoreDependency)) {
			List<Dependency> availableVersions = dependencyFacet.resolveAvailableVersions(wicketCoreDependency);
			Dependency chosenWicketCoreDependency = prompt.promptChoiceTyped(
					"Which version of Apache Wicket do you want to install?", availableVersions,
					availableVersions.get(availableVersions.size() - 1));

			dependencyFacet.setProperty(WICKET_VERSION_PROPERTY_NAME, chosenWicketCoreDependency.getVersion());
			dependencyFacet.addDirectDependency(DependencyBuilder.create(chosenWicketCoreDependency).setVersion(
					"${" + WICKET_VERSION_PROPERTY_NAME + "}"));
		} else {
			out.println("The Apache Wicket Core dependency is already in your pom.");
		}
	}

	private DependencyBuilder createWicketCoreDependency() {
		return DependencyBuilder.create().setGroupId(WICKET_GROUP_ID).setArtifactId("wicket-core")
				.setScopeType(ScopeType.COMPILE);
	}
}
