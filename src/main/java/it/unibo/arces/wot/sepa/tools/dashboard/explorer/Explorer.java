package it.unibo.arces.wot.sepa.tools.dashboard.explorer;

import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.QueryResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTerm;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.GenericClient;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.tools.dashboard.tableModels.GraphTableModel;
import it.unibo.arces.wot.sepa.tools.dashboard.tableModels.InstanceTableModel;

public class Explorer {
	private static final Logger logger = LogManager.getLogger();

	private JTree explorerTree;
	private JTable graphsTable;
	private GenericClient sepaClient;
	private GraphTableModel graphs;
	private JTextField nRetry;
	private JTextField timeout;
	private ArrayList<RDFTerm> navStack;
	private JLabel currentSubject;
	private InstanceTableModel tableInstancePropertiesDataModel;
	private JTable tableInstanceProperties;
	private JButton buttonStackBackward;
	private JLabel graphsEndpointLabel;
	private JSAP appProfile;

	public Explorer(JTree explorerTree, JTable graphsTable, GenericClient sepaClient, GraphTableModel graphs,
			JTextField nRetry, JTextField timeout, ArrayList<RDFTerm> navStack, JLabel currentSubject,
			InstanceTableModel tableInstancePropertiesDataModel, JTable tableInstanceProperties,
			JButton buttonStackBackward, JLabel graphsEndpointLabel, JSAP appProfile) {
		this.explorerTree = explorerTree;
		this.graphsTable = graphsTable;
		this.sepaClient = sepaClient;
		this.graphs = graphs;
		this.nRetry = nRetry;
		this.timeout = timeout;
		this.navStack = navStack;
		this.currentSubject = currentSubject;
		this.tableInstancePropertiesDataModel = tableInstancePropertiesDataModel;
		this.tableInstanceProperties = tableInstanceProperties;
		this.buttonStackBackward = buttonStackBackward;
		this.graphsEndpointLabel = graphsEndpointLabel;
		this.appProfile = appProfile;
	}

	public void onExplorerSelectTreeElement(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) explorerTree.getLastSelectedPathComponent();
		DefaultTreeModel model = (DefaultTreeModel) explorerTree.getModel();

		if (node == null)
			// Nothing is selected.
			return;

		if (node.isRoot())
			return;

		Bindings nodeInfo = (Bindings) node.getUserObject();

		RDFTerm graph = new RDFTermURI(
				(String) graphs.getValueAt(graphsTable.convertRowIndexToModel(graphsTable.getSelectedRow()), 0));

		if (nodeInfo.getValue("class") != null) {
			node.removeAllChildren();
			model.reload();

			Bindings forced = new Bindings();
			forced.addBinding("top", new RDFTermURI(nodeInfo.getValue("class")));
			forced.addBinding("graph", graph);

			try {
				if (sepaClient == null) {
					JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				Response retResponse = sepaClient.query("___DASHBOARD_SUB_CLASSES", forced,
						Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));

				if (retResponse.isError()) {
					logger.error(retResponse);
				} else {
					QueryResponse resultsQueryResponse = (QueryResponse) retResponse;

					for (Bindings valueBindings : resultsQueryResponse.getBindingsResults().getBindings()) {
						if (valueBindings.isURI("class"))
							model.insertNodeInto(new DefaultMutableTreeNode(valueBindings), node, node.getChildCount());
					}
				}

				forced = new Bindings();
				forced.addBinding("class", new RDFTermURI(nodeInfo.getValue("class")));
				forced.addBinding("graph", graph);

				if (sepaClient == null) {
					JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				retResponse = sepaClient.query("___DASHBOARD_INDIVIDUALS", forced, Integer.parseInt(timeout.getText()),
						Integer.parseInt(nRetry.getText()));
				if (retResponse.isError()) {
					logger.error(retResponse);
				} else {
					QueryResponse resultsQueryResponse = (QueryResponse) retResponse;

					for (Bindings valueBindings : resultsQueryResponse.getBindingsResults().getBindings()) {
						if (valueBindings.isURI("instance") || valueBindings.isBNode("instance"))
							model.insertNodeInto(new DefaultMutableTreeNode(valueBindings), node, node.getChildCount());
					}
				}

			} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException
					| SEPABindingsException e1) {
				logger.error(e1.getMessage());
				if (logger.isTraceEnabled())
					e1.printStackTrace();
			}
		} else if (nodeInfo.getValue("instance") != null) {
			try {
				if (nodeInfo.isBNode("instance")) {
					Bindings classInfo = (Bindings) ((DefaultMutableTreeNode) node.getParent()).getUserObject();

					RDFTerm parent = new RDFTermURI(classInfo.getValue("class"));

					Bindings forced = new Bindings();
					forced.addBinding("parent", parent);
					forced.addBinding("graph", graph);

					currentSubject.setText("BNODE");
					navStack.clear();

					Response retResponse;
					try {
						if (sepaClient == null) {
							JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}

						retResponse = sepaClient.query("___DASHBOARD_BNODE_GRAPH", forced,
								Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));
						if (retResponse.isError()) {
							logger.error(retResponse);
						} else {
							QueryResponse resultsQueryResponse = (QueryResponse) retResponse;

							tableInstancePropertiesDataModel.clear();
							for (Bindings valueBindings : resultsQueryResponse.getBindingsResults().getBindings()) {
								if (nodeInfo.getValue("instance").equals(valueBindings.getValue("subject")))
									tableInstancePropertiesDataModel.addRow(valueBindings);
							}
						}
					} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException
							| SEPABindingsException e1) {
						logger.error(e1.getMessage());
						if (logger.isTraceEnabled())
							e1.printStackTrace();
					}

				} else {
					RDFTerm sub = new RDFTermURI(nodeInfo.getValue("instance"));

					Bindings forced = new Bindings();
					forced.addBinding("subject", sub);
					forced.addBinding("graph", graph);

					currentSubject.setText(sub.getValue());
					navStack.clear();

					Response retResponse;
					try {
						if (sepaClient == null) {
							JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}

						retResponse = sepaClient.query("___DASHBOARD_URI_GRAPH", forced,
								Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));
						if (retResponse.isError()) {
							logger.error(retResponse);
						} else {
							QueryResponse resultsQueryResponse = (QueryResponse) retResponse;

							tableInstancePropertiesDataModel.clear();
							for (Bindings valueBindings : resultsQueryResponse.getBindingsResults().getBindings()) {
								tableInstancePropertiesDataModel.addRow(valueBindings);
							}
						}
					} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException
							| SEPABindingsException e1) {
						logger.error(e1.getMessage());
						if (logger.isTraceEnabled())
							e1.printStackTrace();
					}
				}
			} catch (HeadlessException e1) {
				logger.error(e1.getMessage());
			} catch (NumberFormatException e1) {
				logger.error(e1.getMessage());
			} catch (SEPABindingsException e1) {
				logger.error(e1.getMessage());
			}
		}
	}

	public void onExplorerBackButton(MouseEvent e) {
		Bindings forced = new Bindings();
		RDFTerm sub = navStack.get(navStack.size() - 1);
		forced.addBinding("subject", sub);

		RDFTerm graph = new RDFTermURI(
				(String) graphs.getValueAt(graphsTable.convertRowIndexToModel(graphsTable.getSelectedRow()), 0));
		forced.addBinding("graph", graph);

		Response retResponse;
		try {
			if (sepaClient == null) {
				JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			retResponse = sepaClient.query("___DASHBOARD_URI_GRAPH", forced, Integer.parseInt(timeout.getText()),
					Integer.parseInt(nRetry.getText()));
			if (retResponse.isError()) {
				logger.error(retResponse);
			} else {
				currentSubject.setText(sub.getValue());
				navStack.remove(navStack.size() - 1);
				if (navStack.isEmpty())
					buttonStackBackward.setVisible(false);

				QueryResponse resultsQueryResponse = (QueryResponse) retResponse;

				tableInstancePropertiesDataModel.clear();
				for (Bindings valueBindings : resultsQueryResponse.getBindingsResults().getBindings()) {
					tableInstancePropertiesDataModel.addRow(valueBindings);
				}
			}
		} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException | SEPABindingsException e1) {
			logger.error(e1.getMessage());
			if (logger.isTraceEnabled())
				e1.printStackTrace();
		}
	}

	public void onExplorerPropertiesNavigation(MouseEvent e) {
		logger.debug(e);
		if (e.getClickCount() == 2) {
			if (!tableInstanceProperties.getValueAt(tableInstanceProperties.getSelectedRow(), 2).equals("URI")
					&& !tableInstanceProperties.getValueAt(tableInstanceProperties.getSelectedRow(), 2).equals("BNODE"))
				return;

			Bindings forced = new Bindings();
			RDFTerm sub = new RDFTermURI(
					(String) tableInstanceProperties.getValueAt(tableInstanceProperties.getSelectedRow(), 1));
			forced.addBinding("subject", sub);
			RDFTerm graph = new RDFTermURI(
					(String) graphs.getValueAt(graphsTable.convertRowIndexToModel(graphsTable.getSelectedRow()), 0));
			forced.addBinding("graph", graph);

			Response retResponse;
			try {
				if (sepaClient == null) {
					JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				retResponse = sepaClient.query("___DASHBOARD_URI_GRAPH", forced, Integer.parseInt(timeout.getText()),
						Integer.parseInt(nRetry.getText()));
				if (retResponse.isError()) {
					logger.error(retResponse);
				} else {
					RDFTerm back = new RDFTermURI(currentSubject.getText());
					navStack.add(back);
					currentSubject.setText(sub.getValue());
					buttonStackBackward.setVisible(true);

					QueryResponse resultsQueryResponse = (QueryResponse) retResponse;

					tableInstancePropertiesDataModel.clear();
					for (Bindings valueBindings : resultsQueryResponse.getBindingsResults().getBindings()) {
						tableInstancePropertiesDataModel.addRow(valueBindings);
					}
				}
			} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException
					| SEPABindingsException e1) {
				logger.error(e1.getMessage());
				if (logger.isTraceEnabled())
					e1.printStackTrace();
			}
		}
	}

	public void onExloperSelectGraph() {
		int row = graphsTable.getSelectedRow();
		String graphUri = (String) graphs.getValueAt(graphsTable.convertRowIndexToModel(row), 0);

		Bindings forced = new Bindings();
		forced.addBinding("graph", new RDFTermURI(graphUri));

		tableInstancePropertiesDataModel.clear();
		tableInstancePropertiesDataModel.fireTableDataChanged();

		currentSubject.setText("");

		try {
			if (sepaClient == null) {
				JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			Response retResponse = sepaClient.query("___DASHBOARD_TOP_CLASSES", forced,
					Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));

			if (retResponse.isError()) {
				logger.error(retResponse);
			} else {
				DefaultTreeModel model = (DefaultTreeModel) explorerTree.getModel();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) model.getRoot();

				node.removeAllChildren();
				model.reload();

				QueryResponse resultsQueryResponse = (QueryResponse) retResponse;
				for (Bindings valueBindings : resultsQueryResponse.getBindingsResults().getBindings()) {
					model.insertNodeInto(new DefaultMutableTreeNode(valueBindings), node, node.getChildCount());
				}
			}
		} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException | SEPABindingsException e1) {
			logger.error(e1.getMessage());
			if (logger.isTraceEnabled())
				e1.printStackTrace();
		}
	}

	public void onExplorerOpenTab(boolean refresh) {
		if (sepaClient == null) {
			JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
					JOptionPane.INFORMATION_MESSAGE);
			graphs.clear();
			return;
		}

		try {
			graphsEndpointLabel.setText(appProfile.getHost());

			if (!refresh && graphs.getRowCount() != 0)
				return;

			if (refresh)
				graphs.clear();

			Response retResponse = sepaClient.query("___DASHBOARD_GRAPHS", null, Integer.parseInt(timeout.getText()),
					Integer.parseInt(nRetry.getText()));
			if (retResponse.isError()) {
				logger.error(retResponse);
			} else {
				QueryResponse response = (QueryResponse) retResponse;
				for (Bindings bindings : response.getBindingsResults().getBindings()) {
					graphs.addRow(bindings.getValue("graph"), Integer.parseInt(bindings.getValue("count")));
				}
			}
		} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException | SEPABindingsException e1) {
			logger.error(e1.getMessage());
			if (logger.isTraceEnabled())
				e1.printStackTrace();
		}
	}

}
