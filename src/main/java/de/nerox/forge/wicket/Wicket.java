package de.nerox.forge.wicket;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
	public void setup(PipeOut out) throws Exception {
		dependencyFacet = project.getFacet(DependencyFacet.class);

		installWicketCoreDependency(out);
		setupWebXml();
	}

	private void setupWebXml() throws Exception {
		WebResourceFacet webResourceFacet = project.getFacet(WebResourceFacet.class);
		FileResource<?> webXml = webResourceFacet.getWebResource("WEB-INF/web.xml");
		if (!webXml.exists()) {
			createWebXmlFromTemplate(webXml);
		}
		addWicketNodesToWebXml(webXml);
	}

	private void createWebXmlFromTemplate(FileResource<?> webXml) throws IOException {
		webXml.createNewFile();
		webXml.setContents(getWebXmlTemplateContent());
	}

	private String getWebXmlTemplateContent() throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("web.xml"));
	}

	private void addWicketNodesToWebXml(FileResource<?> webXmlResource) throws Exception {
		String projectName = project.getProjectRoot().toString();
		Node webXml = XMLParser.parse(webXmlResource.getResourceInputStream());

		addDisplayName(projectName, webXml);
		addContextParam(webXml);
		addFilter(projectName, webXml);

		webXmlResource.setContents(XMLParser.toXMLInputStream(webXml));
	}

	private void addFilter(String projectName, Node webXml) {
		List<Node> contextParams = webXml.get("filter");
		for (Node contextParam : contextParams) {
			Node paramName = contextParam.getSingle("filter-class");
			if (paramName.getText().equals("org.apache.wicket.protocol.http.WicketFilter")) {
				return;
			}
		}

		Node filter = webXml.createChild("filter");
		filter.createChild("filter-name").text("wicket." + projectName);
		filter.createChild("filter-class").text("org.apache.wicket.protocol.http.WicketFilter");
		Node initParam = filter.createChild("init-param");
		initParam.createChild("param-name").text("applicationClassName");
		// TODO
		initParam.createChild("param-value").text("TODO");

		addFilterMapping(projectName, webXml);
	}

	private void addFilterMapping(String projectName, Node webXml) {
		Node filterMapping = webXml.createChild("filter-mapping");
		filterMapping.createChild("filter-name").text("wicket." + projectName);
		filterMapping.createChild("url-pattern").text("/*");
	}

	private void addContextParam(Node webXml) {
		List<Node> contextParams = webXml.get("context-param");
		for (Node contextParam : contextParams) {
			Node paramName = contextParam.getSingle("param-name");
			if (paramName.getText().equals("configuration")) {
				return;
			}
		}

		Node contextParam = webXml.createChild("context-param");
		contextParam.createChild("description").text(
				"Configures Apache Wickets to either run in develoment or in deployment mode");
		contextParam.createChild("param-name").text("configuration");
		contextParam.createChild("param-value").text("development");
	}

	private void addDisplayName(String projectName, Node webXml) {
		Node displayName = webXml.getOrCreate("display-name");
		if (StringUtils.isBlank(displayName.getText())) {
			displayName.text(projectName);
		}
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
