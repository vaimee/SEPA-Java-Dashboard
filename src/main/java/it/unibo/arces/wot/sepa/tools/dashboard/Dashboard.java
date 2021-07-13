/* This GUI can be used for debugging SEPA applications
 * 
 * Author: Luca Roffia (luca.roffia@unibo.it)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.unibo.arces.wot.sepa.tools.dashboard;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.datatype.DatatypeFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JPanel;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JTabbedPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSplitPane;

import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.GenericClient;
import it.unibo.arces.wot.sepa.api.ISubscriptionHandler;
import it.unibo.arces.wot.sepa.api.SPARQL11SEProperties.SubscriptionProtocol;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.protocol.SPARQL11Properties.QueryHTTPMethod;
import it.unibo.arces.wot.sepa.commons.protocol.SPARQL11Properties.UpdateHTTPMethod;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.Notification;
import it.unibo.arces.wot.sepa.commons.response.QueryResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.security.OAuthProperties;
import it.unibo.arces.wot.sepa.commons.security.ClientSecurityManager;
import it.unibo.arces.wot.sepa.commons.sparql.ARBindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTerm;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermBNode;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;

import java.awt.event.KeyEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.LineBorder;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.awt.Panel;
import java.awt.Rectangle;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class Dashboard implements LoginListener {
	private static final Logger logger = LogManager.getLogger();

	static final String title = "SEPA Dashboard Ver 0.9.11";

	static Dashboard window;

	private GenericClient sepaClient;
	private DashboardHandler handler = new DashboardHandler();
	private JSAP appProfile = null;
	private Properties appProperties = new Properties();
	private OAuthProperties oauth = null;
	private ClientSecurityManager sm;

	private DefaultTableModel namespacesDM;
	private String namespacesHeader[] = new String[] { "Prefix", "URI" };

	private BindingsTableModel bindingsDM = new BindingsTableModel();
	private BindingsRender bindingsRender = new BindingsRender();
	private InstanceTableModel tableInstancePropertiesDataModel = new InstanceTableModel();
	private GraphTableModel graphs = new GraphTableModel();

	private ForcedBindingsTableModel updateForcedBindingsDM = new ForcedBindingsTableModel();
	private ForcedBindingsTableModel subscribeForcedBindingsDM = new ForcedBindingsTableModel();

	private SortedListModel updateListDM = new SortedListModel();
	private SortedListModel queryListDM = new SortedListModel();
	private SortedListModel jsapListDM = new SortedListModel();

	private HashMap<String, JPanel> subscriptions = new HashMap<String, JPanel>();
	private HashMap<String, BindingsTableModel> subscriptionResultsDM = new HashMap<String, BindingsTableModel>();
	private HashMap<String, JLabel> subscriptionResultsLabels = new HashMap<String, JLabel>();
	private HashMap<String, JTable> subscriptionResultsTables = new HashMap<String, JTable>();

	private JTextArea updateSPARQL;
	private JTextArea querySPARQL;

	private DefaultTableModel propertiesDM;
	private String propertiesHeader[] = new String[] { "Property", "Domain", "Range", "Comment" };

	private JFrame frmSepaDashboard;

	private Panel sparqlTab;

	private JTable namespacesTable;
	private JTable bindingsResultsTable;
	private JTable updateForcedBindings;
	private JTable queryForcedBindings;
	private JLabel updateURL;
	private JLabel usingGraphURI;
	private JLabel usingNamedGraphURI;
	private JLabel defaultGraphURI;
	private JLabel namedGraphURI;
	private JLabel subscribeURL;
	private JLabel queryURL;

	private JButton updateButton;
	private JButton subscribeButton;

	private String updateID;
	private String queryID;

	private JList<String> queryList;
	private JList<String> updateList;

	private JTabbedPane mainTabs;
	private JTextField timeout;

	private JTextArea textArea;

	private JButton btnQuery;
	private JLabel updateInfo;
	private JLabel queryInfo;
	private JCheckBox chckbxMerge;

	private JTabbedPane subscriptionsPanel = new JTabbedPane(JTabbedPane.TOP);

	private Login login = null;
	private JButton btnLogin;

	private ArrayList<String> jsapFiles = new ArrayList<String>();

	private JTree explorerTree;
	private JTable tableInstanceProperties;
	private ArrayList<RDFTerm> navStack = new ArrayList<RDFTerm>();

	private JLabel currentSubject;
	private JTable graphsTable;
	private JButton buttonStackBackward;
	private JCheckBox chckbxDatatype;
	private JCheckBox chckbxQname;
	private JLabel graphsEndpointLabel;

	private boolean signedIn = false;
	private JTextField nRetry;

	class DashboardHandler implements ISubscriptionHandler {
		protected String unsubSpuid;

		@Override
		public void onSemanticEvent(Notification n) {
			ARBindingsResults notify = n.getARBindingsResults();
			String spuid = n.getSpuid();

			int added = 0;
			int removed = 0;

			if (notify != null) {

				if (notify.getAddedBindings() != null)
					added = notify.getAddedBindings().size();
				if (notify.getRemovedBindings() != null)
					removed = notify.getRemovedBindings().size();

				try {
					subscriptionResultsDM.get(spuid).setResults(notify, spuid);
				} catch (SEPABindingsException e) {
					logger.error(e.getMessage());
				}

				subscriptionResultsLabels.get(spuid)
						.setText("Bindings results (" + subscriptionResultsDM.get(spuid).getRowCount() + ") Added("
								+ added + ") + Removed (" + removed + ")");
			}

		}

		@Override
		public void onBrokenConnection(ErrorResponse err) {
			logger.error("*** BROKEN CONNECTION *** " + err);
		}

		@Override
		public void onError(ErrorResponse errorResponse) {
			logger.error(errorResponse);
		}

		@Override
		public void onSubscribe(String spuid, String alias) {
			// Subscription panel
			JPanel sub = new JPanel();

			// Layout
			GridBagConstraints layoutFill = new GridBagConstraints();
			layoutFill.fill = GridBagConstraints.BOTH;
			sub.setLayout(new BoxLayout(sub, BoxLayout.Y_AXIS));
			sub.setName(queryList.getSelectedValue());

			// Query label
			JLabel queryLabel = new JLabel("<html>" + querySPARQL.getText() + "</html>");
			queryLabel.setFont(new Font("Arial", Font.BOLD, 14));

			// Info label
			JLabel info = new JLabel("Info");
			info.setText(spuid);
			subscriptionResultsLabels.put(spuid, info);

			// Unsubscribe button
			JButton unsubscribeButton = new JButton(spuid);
			unsubscribeButton.setEnabled(true);
			unsubscribeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
//						subOp = SUB_OP.UNSUB;
						unsubSpuid = spuid;
						if (sepaClient == null) {
							JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						sepaClient.unsubscribe(spuid, Integer.parseInt(timeout.getText()),
								Integer.parseInt(nRetry.getText()));
						new Thread() {
							public void run() {
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									return;
								}
								if (subscriptions.containsKey(spuid)) {
									logger.warn("Force unsubscribe");
									onUnsubscribe(spuid);
								}
							}
						}.start();
					} catch (NumberFormatException | SEPASecurityException | SEPAPropertiesException
							| SEPAProtocolException | InterruptedException e1) {
						logger.error(e1.getMessage());
					}
				}
			});

			// Results table
			subscriptionResultsDM.put(spuid, new BindingsTableModel());

			JTable bindingsResultsTable = new JTable(subscriptionResultsDM.get(spuid));
			JScrollPane bindingsResults = new JScrollPane();
			bindingsResults.setViewportView(bindingsResultsTable);

			bindingsResultsTable.setDefaultRenderer(Object.class, bindingsRender);
			bindingsResultsTable.setAutoCreateRowSorter(true);
			bindingsResultsTable.registerKeyboardAction(new CopyAction(),
					KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
					JComponent.WHEN_FOCUSED);
			bindingsResultsTable.setCellSelectionEnabled(true);

			subscriptionResultsTables.put(spuid, bindingsResultsTable);

			// Add all elements
			sub.add(queryLabel);
			sub.add(unsubscribeButton);
			sub.add(bindingsResults);
			sub.add(info);

			// Add tab
			subscriptionsPanel.add(sub, layoutFill);

			subscriptionsPanel.setSelectedIndex(subscriptionsPanel.getTabCount() - 1);
			mainTabs.setSelectedIndex(1);

			subscriptions.put(spuid, sub);

		}

		@Override
		public void onUnsubscribe(String spuid) {
			if (subscriptions.containsKey(spuid)) {
				subscriptionsPanel.remove(subscriptions.get(spuid));
				subscriptions.remove(spuid);
				subscriptionResultsDM.remove(spuid);
				subscriptionResultsLabels.remove(spuid);
				subscriptionResultsTables.remove(spuid);
			}
		}
	}

	private class SortedListModel extends AbstractListModel<String> {

		/**
		* 
		*/
		private static final long serialVersionUID = -4860350252985388420L;

		SortedSet<String> model;

		public SortedListModel() {
			model = new TreeSet<String>();
		}

		public int getSize() {
			return model.size();
		}

		public String getElementAt(int index) {
			return (String) model.toArray()[index];
		}

		public void add(String element) {
			if (model.add(element)) {
				fireContentsChanged(this, 0, getSize());
			}
		}

		public void clear() {
			model.clear();
			fireContentsChanged(this, 0, getSize());
		}
	}

	private class CopyAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5927169526678239559L;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable tbl = (JTable) e.getSource();

			StringBuffer sbf = new StringBuffer();

			int[] rowsselected = tbl.getSelectedRows();
			int[] colsselected = tbl.getSelectedColumns();

			for (int i = 0; i < rowsselected.length; i++) {
				for (int j = 0; j < colsselected.length; j++) {
					TableCellRenderer renderer = tbl.getCellRenderer(rowsselected[i], colsselected[j]);
					final Component comp = tbl.prepareRenderer(renderer, rowsselected[i], colsselected[j]);
					String toCopy = ((JLabel) comp).getText();
					sbf.append(toCopy);
					if (j < colsselected.length - 1)
						sbf.append("\t");
				}
				if (i < rowsselected.length - 1)
					sbf.append("\n");
			}
			StringSelection stsel = new StringSelection(sbf.toString());
			Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
			system.setContents(stsel, stsel);
		}

	}

	private class ForcedBindingsTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8524602022439421892L;

		ArrayList<String[]> rowValues = new ArrayList<String[]>();
		ArrayList<String> rowTypes = new ArrayList<String>();
		ArrayList<String> columns = new ArrayList<String>();

		public void clearBindings() {
			rowValues.clear();
			rowTypes.clear();

			super.fireTableDataChanged();
		}

		public void addBindings(String variable, String literal, String value) {
			if (value != null)
				rowValues.add(new String[] { variable, value });
			else
				rowValues.add(new String[] { variable, "" });
			rowTypes.add(literal);

			super.fireTableDataChanged();
		}

		public ForcedBindingsTableModel() {
			columns.add("Variable");
			columns.add("Value");
			columns.add("Datatype");
		}

		@Override
		public int getRowCount() {
			return rowValues.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0 || columnIndex == 1)
				return rowValues.get(rowIndex)[columnIndex];
			return rowTypes.get(rowIndex);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex < getColumnCount())
				return columns.get(columnIndex);
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 1)
				return true;
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			super.setValueAt(aValue, rowIndex, columnIndex);

			if (rowIndex > rowValues.size() - 1)
				return;

			if (columnIndex == 1) {
				String[] currentValue = rowValues.get(rowIndex);
				currentValue[1] = (String) aValue;
				rowValues.set(rowIndex, currentValue);
			}
			if (columnIndex == 2)
				rowTypes.set(rowIndex, (String) aValue);

			super.fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			super.addTableModelListener(l);
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			super.removeTableModelListener(l);
		}

	}

	private class BindingValue implements Comparable<BindingValue> {
		private boolean added = true;
		private String value;
		private boolean literal = true;
		private String dataType = null;

		public BindingValue(String value, boolean literal, String dataType, boolean added) {
			this.value = value;
			this.added = added;
			this.literal = literal;
			this.dataType = dataType;
		}

		public boolean isAdded() {
			return added;

		}

		public String get() {
			return value;
		}

		public String getDataType() {
			return dataType;
		}

		public boolean isLiteral() {
			return literal;
		}

		@Override
		public int compareTo(BindingValue o) {
			return value.compareTo(o.get());
		}
	}

	private class BindingsTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 2698789913874225961L;

		ArrayList<HashMap<String, BindingValue>> rows = new ArrayList<HashMap<String, BindingValue>>();
		ArrayList<String> columns = new ArrayList<String>();

		public void clear() {
			columns.clear();
			rows.clear();

			super.fireTableStructureChanged();
			super.fireTableDataChanged();
		}

		public void setResults(ARBindingsResults res, String spuid) throws SEPABindingsException {
			if (res == null)
				return;

			ArrayList<String> vars = res.getAddedBindings().getVariables();
			for (String var : res.getRemovedBindings().getVariables()) {
				if (!vars.contains(var))
					vars.add(var);
			}

			if (!columns.containsAll(vars) || columns.size() != vars.size()) {
				columns.clear();
				columns.addAll(vars);
				super.fireTableStructureChanged();
			}

			if (res.getRemovedBindings() != null) {
				for (Bindings sol : res.getRemovedBindings().getBindings()) {
					HashMap<String, BindingValue> row = new HashMap<String, BindingValue>();
					for (String var : sol.getVariables()) {
						row.put(var,
								new BindingValue(sol.getValue(var), sol.isLiteral(var), sol.getDatatype(var), false));
					}
					rows.add(row);
				}
			}

			if (res.getAddedBindings() != null) {
				for (Bindings sol : res.getAddedBindings().getBindings()) {
					HashMap<String, BindingValue> row = new HashMap<String, BindingValue>();
					for (String var : sol.getVariables()) {
						row.put(var,
								new BindingValue(sol.getValue(var), sol.isLiteral(var), sol.getDatatype(var), true));
					}
					rows.add(row);
				}
			}

			subscriptionResultsTables.get(spuid).changeSelection(subscriptionResultsTables.get(spuid).getRowCount() - 1,
					0, false, false);

			super.fireTableDataChanged();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return BindingValue.class;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return columns.size();
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex < getColumnCount())
				return columns.get(columnIndex);
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			BindingValue ret = null;
			if (rowIndex < getRowCount() && columnIndex < getColumnCount()) {
				ret = rows.get(rowIndex).get(columns.get(columnIndex));
			}
			return ret;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			super.setValueAt(aValue, rowIndex, columnIndex);

			super.fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			super.addTableModelListener(l);
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			super.removeTableModelListener(l);
		}

		public void setAddedResults(BindingsResults bindingsResults, String spuid) throws SEPABindingsException {
			if (bindingsResults == null)
				return;

			ArrayList<String> vars = bindingsResults.getVariables();

			if (!columns.containsAll(vars) || columns.size() != vars.size()) {
				columns.clear();
				columns.addAll(vars);
				super.fireTableStructureChanged();
			}

			for (Bindings sol : bindingsResults.getBindings()) {
				HashMap<String, BindingValue> row = new HashMap<String, BindingValue>();
				for (String var : sol.getVariables()) {
					row.put(var, new BindingValue(sol.getValue(var), sol.isLiteral(var), sol.getDatatype(var), true));
				}
				rows.add(row);
			}

			if (subscriptionResultsTables.get(spuid) != null)
				subscriptionResultsTables.get(spuid)
						.changeSelection(subscriptionResultsTables.get(spuid).getRowCount() - 1, 0, false, false);

			super.fireTableDataChanged();
		}
	}

	private class GraphTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -72022807754650051L;
		private ArrayList<String> uriArrayList = new ArrayList<>();
		private ArrayList<Integer> counterArrayList = new ArrayList<>();

		private String[] columnStrings = { "Named graph URI", "Triples" };

		public String getColumnName(int col) {
			return columnStrings[col];
		}

		@Override
		public int getRowCount() {
			return uriArrayList.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return uriArrayList.get(rowIndex);
			return counterArrayList.get(rowIndex);
		}

		public void addRow(String uri, Integer count) {
			uriArrayList.add(uri);
			counterArrayList.add(count);
			fireTableDataChanged();
		}

		public void clear() {
			uriArrayList.clear();
			;
			counterArrayList.clear();
			;
			fireTableDataChanged();
		}
	}

	private class InstanceTableModel extends AbstractTableModel {

		private String[] columnStrings = { "Predicate", "Object", "Datatype" };
		private ArrayList<Bindings> rows = new ArrayList<Bindings>();

		private static final long serialVersionUID = 1L;

		public String getColumnName(int col) {
			return columnStrings[col];
		}

		public int getRowCount() {
			return rows.size();
		}

		public int getColumnCount() {
			return columnStrings.length;
		}

		public Object getValueAt(int row, int col) {
			if (col == 0)
				return rows.get(row).getValue("predicate");
			else if (col == 1)
				return rows.get(row).getValue("object");
			else {
				try {
					if (rows.get(row).getRDFTerm("object").isURI())
						return "URI";
					if (rows.get(row).getRDFTerm("object").isBNode())
						return "BNODE";
				} catch (SEPABindingsException e) {
					logger.error(e.getMessage());
					return "???";
				}
				return rows.get(row).getDatatype("object");
			}
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 1)
				return true;
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			Bindings newBindings = new Bindings();

			String graphString = (String) graphs.getValueAt(graphsTable.getSelectedRow(), 0);

			newBindings.addBinding("graph", new RDFTermURI(graphString));

			try {
				newBindings.addBinding("predicate", rows.get(row).getRDFTerm("predicate"));
				newBindings.addBinding("subject", new RDFTermURI(currentSubject.getText()));

				if (rows.get(row).getRDFTerm("object").isLiteral()) {
					newBindings.addBinding("object", new RDFTermLiteral((String) value));
					if (sepaClient == null) {
						JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					sepaClient.update("___DASHBOARD_UPDATE_LITERAL", newBindings, Integer.parseInt(timeout.getText()),
							Integer.parseInt(nRetry.getText()));
				} else {
					newBindings.addBinding("object", new RDFTermURI((String) value));
					if (sepaClient == null) {
						JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					sepaClient.update("___DASHBOARD_UPDATE_URI", newBindings, Integer.parseInt(timeout.getText()),
							Integer.parseInt(nRetry.getText()));
				}
			} catch (SEPABindingsException | SEPAProtocolException | SEPASecurityException | IOException
					| SEPAPropertiesException e) {
				logger.error(e.getMessage());
				if (logger.isTraceEnabled())
					e.printStackTrace();
				return;
			}
			rows.remove(row);
			rows.add(row, newBindings);
			fireTableCellUpdated(row, col);
		}

		public void clear() {
			rows.clear();
			super.fireTableDataChanged();
		}

		public void addRow(Bindings b) {
			rows.add(b);
		}

	}

	private class ForcedBindingsRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1541296097107576037L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {

			// Cells are by default rendered as a JLabel.
			JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			if (col == 2) {
				String v = (String) table.getValueAt(row, 1);
				String type = (String) table.getValueAt(row, 2);
				logger.trace("Row: " + row + " Col: " + col + " Value: " + v + " Type: " + type);
				if (type == null)
					l.setBackground(Color.WHITE);
				else if (checkType(v, type)) {
					if (v.equals(""))
						l.setBackground(Color.ORANGE);
					else
						l.setBackground(Color.GREEN);
				} else
					l.setBackground(Color.RED);
			} else
				l.setBackground(Color.WHITE);

			l.setForeground(Color.BLACK);

			return l;
		}
	}

	private class BindingsRender extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 3932800852596396532L;

		DefaultTableModel namespaces;
		private boolean showAsQname = true;
		private boolean showDataType = true;

		public BindingsRender() {
			super();
		}

		public void setNamespaces(DefaultTableModel namespaces) {
			this.namespaces = namespaces;
		}

		public void showAsQName(boolean set) {
			showAsQname = set;
		}

		public void showDataType(boolean show) {
			showDataType = show;
		}

		private String qName(String value, boolean literal, String dataType) {
			if (namespaces == null)
				return value;
			if (value == null)
				return null;

			if (!literal) {
				for (int row = 0; row < namespaces.getRowCount(); row++) {
					String prefix = namespaces.getValueAt(row, 0).toString();
					String ns = namespaces.getValueAt(row, 1).toString();
					if (value.startsWith(ns))
						return value.replace(ns, prefix + ":");
				}
			} else if (dataType != null && showDataType) {
				for (int row = 0; row < namespaces.getRowCount(); row++) {
					String prefix = namespaces.getValueAt(row, 0).toString();
					String ns = namespaces.getValueAt(row, 1).toString();
					if (dataType.startsWith(ns)) {
						dataType = dataType.replace(ns, prefix + ":");
						break;
					}
				}
				return value + "^^" + dataType;
			}
			return value;
		}

		@Override
		public void setValue(Object value) {
			super.setValue(value);

			if (value == null)
				return;

			BindingValue binding = (BindingValue) value;

			if (binding.isLiteral()) {
				setFont(new Font(null, Font.BOLD, 12));
				setForeground(Color.BLACK);
			} else {
				setFont(new Font(null, Font.PLAIN, 12));
				setForeground(Color.BLACK);
			}
			if (binding.isAdded()) {
				setBackground(Color.WHITE);
			} else
				setBackground(Color.LIGHT_GRAY);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			BindingValue binding = (BindingValue) value;

			if (binding == null) {
				setText("");
				return this;
			}
			if (binding.get() == null) {
				setText("");
				return this;
			}

			// Render as qname or URI
			if (showAsQname)
				setText(qName(binding.get(), binding.isLiteral(), binding.getDataType()));
			else if (binding.isLiteral() && binding.getDataType() != null && showDataType)
				setText(binding.get() + "^^" + binding.getDataType());
			else
				setText(binding.get());

			if (isSelected) {
				this.setBackground(Color.YELLOW);
			}

			return this;
		}
	}

	private class ExplorerTreeModel extends DefaultTreeModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7082219083126306248L;

		public ExplorerTreeModel(TreeNode root) {
			super(root);
		}

		public ExplorerTreeModel() {
			this(new DefaultMutableTreeNode("owl:Thing") {
				/**
				 * 
				 */
				private static final long serialVersionUID = -2640698448157863184L;

				{
				}
			});
		}

	}

	class ExplorerTreeRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4538238852715730476L;

		DefaultTableModel namespaces;

		public void setNamespaces(DefaultTableModel namespaces) {
			this.namespaces = namespaces;
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if (row == 0)
				return this;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Bindings nodeInfo = (Bindings) (node.getUserObject());

			String text = "";
			if (nodeInfo.getValue("class") != null) {
				text = nodeInfo.getValue("class");
			} else {
				text = nodeInfo.getValue("instance");
			}
			setToolTipText(text);

			if (chckbxQname.isSelected()) {
				for (int r = 0; r < namespaces.getRowCount(); r++) {
					String prefix = namespaces.getValueAt(r, 0).toString();
					String ns = namespaces.getValueAt(r, 1).toString();
					if (text.startsWith(ns)) {
						text = text.replace(ns, prefix + ":");
						break;
					}
				}
			}
			setText(text);

			return this;
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new Dashboard();
					window.frmSepaDashboard.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws SEPAPropertiesException
	 * @throws SEPASecurityException
	 * @throws URISyntaxException
	 * 
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchPaddingException
	 * @throws ClassCastException
	 * @throws NullPointerException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalArgumentException
	 */
	public Dashboard() throws SEPAPropertiesException, SEPASecurityException, URISyntaxException {
		initialize();

		loadSAP(null, true);
	}

	private void loadDashboardProperties() throws IOException {
		FileInputStream in = new FileInputStream("dashboard.properties");
		appProperties.load(in);
	}

	protected boolean loadSAP(String file, boolean load) {
		namespacesDM.getDataVector().clear();
		updateListDM.clear();
		queryListDM.clear();

		updateForcedBindingsDM.clearBindings();
		subscribeForcedBindingsDM.clearBindings();

		queryURL.setText("-");
		updateURL.setText("-");

		defaultGraphURI.setText("-");
		namedGraphURI.setText("-");
		usingGraphURI.setText("-");
		usingNamedGraphURI.setText("-");
		subscribeURL.setText("-");

		queryList.clearSelection();
		updateList.clearSelection();

		querySPARQL.setText("");
		updateSPARQL.setText("");

		bindingsDM.clear();

		updateButton.setEnabled(false);
		subscribeButton.setEnabled(false);

		if (file == null) {
			try {
				loadDashboardProperties();
			} catch (IOException e) {
				logger.warn(e.getMessage());
				try {
					return onLoadJSAPButton();
				} catch (SEPASecurityException | URISyntaxException e1) {
					logger.error(e1.getMessage());
					return false;
				}
			}

			// LOAD properties
			String path = appProperties.getProperty("appProfile");

			if (path == null) {
				logger.error("Path in dashboard.properties is null");
				return false;
			}

			String[] jsaps = path.split(",");
			jsapListDM.clear();

			for (int i = 0; i < jsaps.length; i++) {
				jsapFiles.add(jsaps[i]);
				jsapListDM.add(jsaps[i]);
			}

			try {
				appProfile = new JSAP(jsapFiles.get(0));
			} catch (SEPAPropertiesException | SEPASecurityException e) {
				logger.error(e.getMessage());
				return false;
			}

			if (jsapFiles.size() > 1) {
				for (int i = 1; i < jsaps.length; i++) {
					try {
						appProfile.read(jsapFiles.get(i), true);
					} catch (SEPAPropertiesException | SEPASecurityException e) {
						logger.error(e.getMessage());
					}
				}
			}
		} else {
			try {
				loadDashboardProperties();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
			try {

				if (load) {
					appProfile = new JSAP(file);
					appProfile.read(file, true);

					jsapFiles.clear();
					jsapFiles.add(file);

					jsapListDM.clear();
					jsapListDM.add(file);

				} else if (appProfile != null) {
					appProfile.read(file, true);
					jsapFiles.add(file);
					jsapListDM.add(file);
				}
			} catch (SEPAPropertiesException | SEPASecurityException e) {
				logger.error(e.getMessage());
				return false;
			}
		}

		// Add explorer JSAP
//		String dashboardJsapUrl;
//		try {
//			dashboardJsapUrl = Dashboard.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
//		} catch (URISyntaxException e2) {
//			logger.error(e2.getMessage());
//			return false;
//		}
//
//		
//		
//		if (dashboardJsapUrl == null) {
//			JOptionPane.showMessageDialog(null, "File explorer.jsap not found",
//					"Warning: Dashboard.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() is null",
//					JOptionPane.INFORMATION_MESSAGE);
//			return false;
//		}

		try {
			appProfile.read(getClass().getClassLoader().getResourceAsStream("explorer.jsap"));
			// appProfile.read(dashboardJsapUrl + "explorer.jsap");
		} catch (SEPAPropertiesException | SEPASecurityException e2) {
			logger.error(e2.getMessage());
			return false;
		}

		// Loading namespaces
		for (String prefix : appProfile.getNamespaces().keySet()) {
			Vector<String> row = new Vector<String>();
			row.add(prefix);
			row.addElement(appProfile.getNamespaces().get(prefix));
			namespacesDM.addRow(row);
		}

		// Loading updates
		for (String update : appProfile.getUpdateIds()) {
			if (update.startsWith("___DASHBOARD_"))
				continue;
			updateListDM.add(update);
		}

		// Loading subscribes
		for (String subscribe : appProfile.getQueryIds()) {
			if (subscribe.startsWith("___DASHBOARD_"))
				continue;
			queryListDM.add(subscribe);
		}

		// Security
		if (appProfile.isSecure()) {
//			try {
//				oauth = new OAuthProperties(appProfile.getFileName());
//			} catch (SEPAPropertiesException | SEPASecurityException e1) {
//				logger.error(e1.getMessage());
//				return false;
//			}

			login = new Login(appProfile.getAuthenticationProperties(), this, frmSepaDashboard);// ,clientIDString,clientSecretString);
			login.setVisible(true);
		} else {

			try {
				sepaClient = new GenericClient(appProfile, handler);
			} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException e) {
				logger.error(e.getMessage());
				return false;
			}
		}

		return true;
	}

	private class DashboardFileFilter extends FileFilter {
		private ArrayList<String> extensions = new ArrayList<String>();
		private String title = "Title";

		public DashboardFileFilter(String title, String ext) {
			super();
			extensions.add(ext);
			this.title = title;
		}

		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			for (String ext : extensions)
				if (f.getName().contains(ext))
					return true;
			return false;
		}

		@Override
		public String getDescription() {
			return title;
		}

	}

	/**
	 * Initialize the contents of the frame.
	 */
	protected void initialize() {
		namespacesDM = new DefaultTableModel(0, 0) {
			private static final long serialVersionUID = 6788045463932990156L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		namespacesDM.setColumnIdentifiers(namespacesHeader);

		propertiesDM = new DefaultTableModel(0, 0) {
			private static final long serialVersionUID = -5161490469556412655L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		propertiesDM.setColumnIdentifiers(propertiesHeader);

		frmSepaDashboard = new JFrame();
		frmSepaDashboard.setFont(new Font("Montserrat", Font.BOLD, 10));
		frmSepaDashboard.setTitle(title);
		frmSepaDashboard.setBounds(100, 100, 925, 768);
		frmSepaDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 465, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 391, -36, 97, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		frmSepaDashboard.getContentPane().setLayout(gridBagLayout);

		JPanel panel_8 = new JPanel();
		GridBagConstraints gbc_panel_8 = new GridBagConstraints();
		gbc_panel_8.insets = new Insets(0, 0, 5, 0);
		gbc_panel_8.fill = GridBagConstraints.BOTH;
		gbc_panel_8.gridx = 0;
		gbc_panel_8.gridy = 0;
		frmSepaDashboard.getContentPane().add(panel_8, gbc_panel_8);
		GridBagLayout gbl_panel_8 = new GridBagLayout();
		gbl_panel_8.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel_8.rowHeights = new int[] { 0, 0 };
		gbl_panel_8.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_8.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_8.setLayout(gbl_panel_8);

		JButton btnLoadXmlProfile = new JButton("Load JSAP");
		GridBagConstraints gbc_btnLoadXmlProfile = new GridBagConstraints();
		gbc_btnLoadXmlProfile.insets = new Insets(0, 0, 0, 5);
		gbc_btnLoadXmlProfile.anchor = GridBagConstraints.WEST;
		gbc_btnLoadXmlProfile.gridx = 0;
		gbc_btnLoadXmlProfile.gridy = 0;
		panel_8.add(btnLoadXmlProfile, gbc_btnLoadXmlProfile);
		btnLoadXmlProfile.setFont(new Font("Montserrat", Font.BOLD, 13));
		btnLoadXmlProfile.setForeground(Color.BLACK);
		btnLoadXmlProfile.setBackground(Color.WHITE);

		chckbxMerge = new JCheckBox("merge");
		GridBagConstraints gbc_chckbxMerge = new GridBagConstraints();
		gbc_chckbxMerge.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxMerge.gridx = 1;
		gbc_chckbxMerge.gridy = 0;
		panel_8.add(chckbxMerge, gbc_chckbxMerge);
		chckbxMerge.setFont(new Font("Montserrat", Font.PLAIN, 11));

		JButton btnNewButton_2 = new JButton("Register");
		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton_2.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_2.gridx = 2;
		gbc_btnNewButton_2.gridy = 0;
		panel_8.add(btnNewButton_2, gbc_btnNewButton_2);

		btnLogin = new JButton("Sign In");
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.anchor = GridBagConstraints.EAST;
		gbc_btnLogin.gridx = 3;
		gbc_btnLogin.gridy = 0;
		panel_8.add(btnLogin, gbc_btnLogin);
		btnLogin.setFont(new Font("Montserrat", Font.BOLD, 13));
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!signedIn)
					login.setVisible(true);
				else {
					btnLogin.setText("Sign in");
					signedIn = false;
					sepaClient = null;
					frmSepaDashboard.setTitle(title);
				}
			}
		});
		btnNewButton_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (signedIn)
					return;

				try {
					oauth = new OAuthProperties(appProfile.getFileName());
				} catch (SEPAPropertiesException | SEPASecurityException e1) {
					logger.error(e1.getMessage());
					return;
				}
				String clientId = oauth.getClientRegistrationId();
				String username = oauth.getUsername();
				String token = oauth.getInitialAccessToken();
				;

				Register dialog = new Register(oauth, frmSepaDashboard, (clientId != null ? clientId : ""),
						(username != null ? username : ""), (token != null ? token : ""));
				dialog.setVisible(true);
			}
		});

		btnLoadXmlProfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					onLoadJSAPButton();
				} catch (SEPASecurityException | URISyntaxException e1) {
					logger.error(e1.getMessage());
				}
			}
		});
		// btnLogin.setEnabled(false);

		mainTabs = new JTabbedPane(JTabbedPane.TOP);
		mainTabs.setFont(new Font("Montserrat", Font.PLAIN, 13));
		GridBagConstraints gbc_mainTabs = new GridBagConstraints();
		gbc_mainTabs.insets = new Insets(0, 0, 5, 0);
		gbc_mainTabs.fill = GridBagConstraints.BOTH;
		gbc_mainTabs.gridx = 0;
		gbc_mainTabs.gridy = 1;
		frmSepaDashboard.getContentPane().add(mainTabs, gbc_mainTabs);

		sparqlTab = new Panel();
		mainTabs.addTab("SPARQL", null, sparqlTab, null);
		mainTabs.setEnabledAt(0, true);
		GridBagLayout gbl_sparqlTab = new GridBagLayout();
		gbl_sparqlTab.columnWidths = new int[] { 420, 0, 0 };
		gbl_sparqlTab.rowHeights = new int[] { 0, 87, 91, 29, 188, 0 };
		gbl_sparqlTab.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_sparqlTab.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		sparqlTab.setLayout(gbl_sparqlTab);

		JPanel updateGraphs = new JPanel();
		updateGraphs.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_updateGraphs = new GridBagConstraints();
		gbc_updateGraphs.anchor = GridBagConstraints.NORTH;
		gbc_updateGraphs.insets = new Insets(0, 0, 5, 5);
		gbc_updateGraphs.fill = GridBagConstraints.HORIZONTAL;
		gbc_updateGraphs.gridx = 0;
		gbc_updateGraphs.gridy = 0;
		sparqlTab.add(updateGraphs, gbc_updateGraphs);
		GridBagLayout gbl_updateGraphs = new GridBagLayout();
		gbl_updateGraphs.columnWidths = new int[] { 0, 0, 0 };
		gbl_updateGraphs.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_updateGraphs.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_updateGraphs.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		updateGraphs.setLayout(gbl_updateGraphs);

		updateURL = new JLabel("-");
		updateURL.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		updateURL.setFont(new Font("Montserrat", Font.BOLD, 12));
		GridBagConstraints gbc_updateURL = new GridBagConstraints();
		gbc_updateURL.gridwidth = 2;
		gbc_updateURL.insets = new Insets(0, 0, 5, 0);
		gbc_updateURL.gridx = 0;
		gbc_updateURL.gridy = 0;
		updateGraphs.add(updateURL, gbc_updateURL);

		JLabel label_2 = new JLabel("using-graph-uri:");
		label_2.setFont(new Font("Montserrat", Font.BOLD, 12));
		label_2.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.anchor = GridBagConstraints.EAST;
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 0;
		gbc_label_2.gridy = 1;
		updateGraphs.add(label_2, gbc_label_2);

		usingGraphURI = new JLabel("-");
		usingGraphURI.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		usingGraphURI.setFont(new Font("Montserrat", Font.BOLD, 12));
		GridBagConstraints gbc_updateUsingGraphURI = new GridBagConstraints();
		gbc_updateUsingGraphURI.anchor = GridBagConstraints.WEST;
		gbc_updateUsingGraphURI.insets = new Insets(0, 0, 5, 0);
		gbc_updateUsingGraphURI.gridx = 1;
		gbc_updateUsingGraphURI.gridy = 1;
		updateGraphs.add(usingGraphURI, gbc_updateUsingGraphURI);

		JLabel label_4 = new JLabel("using-named-graph-uri:");
		label_4.setFont(new Font("Montserrat", Font.BOLD, 12));
		label_4.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		GridBagConstraints gbc_label_4 = new GridBagConstraints();
		gbc_label_4.anchor = GridBagConstraints.EAST;
		gbc_label_4.insets = new Insets(0, 0, 0, 5);
		gbc_label_4.gridx = 0;
		gbc_label_4.gridy = 2;
		updateGraphs.add(label_4, gbc_label_4);

		usingNamedGraphURI = new JLabel("-");
		usingNamedGraphURI.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		usingNamedGraphURI.setFont(new Font("Montserrat", Font.BOLD, 12));
		GridBagConstraints gbc_updateUsingNamedGraphURI = new GridBagConstraints();
		gbc_updateUsingNamedGraphURI.anchor = GridBagConstraints.WEST;
		gbc_updateUsingNamedGraphURI.gridx = 1;
		gbc_updateUsingNamedGraphURI.gridy = 2;
		updateGraphs.add(usingNamedGraphURI, gbc_updateUsingNamedGraphURI);

		JPanel queryGraphs = new JPanel();
		queryGraphs.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_queryGraphs = new GridBagConstraints();
		gbc_queryGraphs.anchor = GridBagConstraints.NORTH;
		gbc_queryGraphs.insets = new Insets(0, 0, 5, 0);
		gbc_queryGraphs.fill = GridBagConstraints.HORIZONTAL;
		gbc_queryGraphs.gridx = 1;
		gbc_queryGraphs.gridy = 0;
		sparqlTab.add(queryGraphs, gbc_queryGraphs);
		GridBagLayout gbl_queryGraphs = new GridBagLayout();
		gbl_queryGraphs.columnWidths = new int[] { 228, 207, 0 };
		gbl_queryGraphs.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_queryGraphs.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_queryGraphs.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		queryGraphs.setLayout(gbl_queryGraphs);

		queryURL = new JLabel("-");
		queryURL.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		queryURL.setFont(new Font("Montserrat", Font.BOLD, 12));
		GridBagConstraints gbc_queryURL = new GridBagConstraints();
		gbc_queryURL.insets = new Insets(0, 0, 5, 5);
		gbc_queryURL.gridx = 0;
		gbc_queryURL.gridy = 0;
		queryGraphs.add(queryURL, gbc_queryURL);

		subscribeURL = new JLabel("-");
		subscribeURL.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		subscribeURL.setFont(new Font("Montserrat", Font.BOLD, 12));
		GridBagConstraints gbc_subscribeURL = new GridBagConstraints();
		gbc_subscribeURL.insets = new Insets(0, 0, 5, 0);
		gbc_subscribeURL.gridx = 1;
		gbc_subscribeURL.gridy = 0;
		queryGraphs.add(subscribeURL, gbc_subscribeURL);

		JLabel label_8 = new JLabel("default-graph-uri:");
		label_8.setFont(new Font("Montserrat", Font.BOLD, 12));
		label_8.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		GridBagConstraints gbc_label_8 = new GridBagConstraints();
		gbc_label_8.anchor = GridBagConstraints.EAST;
		gbc_label_8.insets = new Insets(0, 0, 5, 5);
		gbc_label_8.gridx = 0;
		gbc_label_8.gridy = 1;
		queryGraphs.add(label_8, gbc_label_8);

		defaultGraphURI = new JLabel("-");
		defaultGraphURI.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		defaultGraphURI.setFont(new Font("Montserrat", Font.BOLD, 12));
		GridBagConstraints gbc_defaultGraphURI = new GridBagConstraints();
		gbc_defaultGraphURI.anchor = GridBagConstraints.WEST;
		gbc_defaultGraphURI.insets = new Insets(0, 0, 5, 0);
		gbc_defaultGraphURI.gridx = 1;
		gbc_defaultGraphURI.gridy = 1;
		queryGraphs.add(defaultGraphURI, gbc_defaultGraphURI);

		JLabel label_10 = new JLabel("named-graph-uri:");
		label_10.setFont(new Font("Montserrat", Font.BOLD, 12));
		label_10.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		GridBagConstraints gbc_label_10 = new GridBagConstraints();
		gbc_label_10.anchor = GridBagConstraints.EAST;
		gbc_label_10.insets = new Insets(0, 0, 0, 5);
		gbc_label_10.gridx = 0;
		gbc_label_10.gridy = 2;
		queryGraphs.add(label_10, gbc_label_10);

		namedGraphURI = new JLabel("-");
		namedGraphURI.setForeground(UIManager.getColor("ComboBox.buttonDarkShadow"));
		namedGraphURI.setFont(new Font("Montserrat", Font.BOLD, 12));
		GridBagConstraints gbc_namedGraphURI = new GridBagConstraints();
		gbc_namedGraphURI.anchor = GridBagConstraints.WEST;
		gbc_namedGraphURI.gridx = 1;
		gbc_namedGraphURI.gridy = 2;
		queryGraphs.add(namedGraphURI, gbc_namedGraphURI);

		JSplitPane updates = new JSplitPane();
		updates.setOrientation(JSplitPane.VERTICAL_SPLIT);
		updates.setResizeWeight(0.5);
		GridBagConstraints gbc_updates = new GridBagConstraints();
		gbc_updates.anchor = GridBagConstraints.NORTH;
		gbc_updates.insets = new Insets(0, 0, 5, 5);
		gbc_updates.fill = GridBagConstraints.HORIZONTAL;
		gbc_updates.gridx = 0;
		gbc_updates.gridy = 1;
		sparqlTab.add(updates, gbc_updates);

		JPanel panel_2 = new JPanel();
		updates.setLeftComponent(panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 66, 0 };
		gbl_panel_2.rowHeights = new int[] { 17, 75, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel label_12 = new JLabel("UPDATES");
		label_12.setForeground(Color.BLACK);
		label_12.setFont(new Font("Montserrat", Font.BOLD, 14));
		GridBagConstraints gbc_label_12 = new GridBagConstraints();
		gbc_label_12.anchor = GridBagConstraints.NORTH;
		gbc_label_12.insets = new Insets(0, 0, 5, 0);
		gbc_label_12.gridx = 0;
		gbc_label_12.gridy = 0;
		panel_2.add(label_12, gbc_label_12);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel_2.add(scrollPane, gbc_scrollPane);

		updateList = new JList<String>();
		updateList.setFont(new Font("Montserrat", Font.PLAIN, 11));
		updateList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				try {
					selectUpdateID(updateList.getSelectedValue());
				} catch (SEPABindingsException e1) {
					logger.error(e1.getMessage());
				}
			}
		});
		updateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		updateList.setModel(updateListDM);
		scrollPane.setViewportView(updateList);

		JPanel panel_3 = new JPanel();
		updates.setRightComponent(panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 101, 0 };
		gbl_panel_3.rowHeights = new int[] { 16, 80, 0 };
		gbl_panel_3.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		JLabel lblForcedBindings = new JLabel("FORCED BINDINGS");
		lblForcedBindings.setFont(new Font("Montserrat", Font.PLAIN, 12));
		lblForcedBindings.setForeground(Color.BLACK);
		GridBagConstraints gbc_lblForcedBindings = new GridBagConstraints();
		gbc_lblForcedBindings.anchor = GridBagConstraints.NORTH;
		gbc_lblForcedBindings.insets = new Insets(0, 0, 5, 0);
		gbc_lblForcedBindings.gridx = 0;
		gbc_lblForcedBindings.gridy = 0;
		panel_3.add(lblForcedBindings, gbc_lblForcedBindings);

		JScrollPane scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 1;
		panel_3.add(scrollPane_2, gbc_scrollPane_2);

		updateForcedBindings = new JTable(updateForcedBindingsDM);
		updateForcedBindings.setFont(new Font("Montserrat", Font.PLAIN, 11));
		updateForcedBindings.setCellSelectionEnabled(true);
		updateForcedBindings.setRowSelectionAllowed(false);
		updateForcedBindings.setFillsViewportHeight(true);
		scrollPane_2.setViewportView(updateForcedBindings);
		updateForcedBindings.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				enableUpdateButton();
			}

		});
		updateForcedBindings.setDefaultRenderer(String.class, new ForcedBindingsRenderer());
		updateForcedBindings.registerKeyboardAction(new CopyAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				JComponent.WHEN_FOCUSED);
		updateForcedBindings.setCellSelectionEnabled(true);

		JSplitPane queries = new JSplitPane();
		queries.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_queries = new GridBagConstraints();
		gbc_queries.anchor = GridBagConstraints.NORTH;
		gbc_queries.insets = new Insets(0, 0, 5, 0);
		gbc_queries.fill = GridBagConstraints.HORIZONTAL;
		gbc_queries.gridx = 1;
		gbc_queries.gridy = 1;
		sparqlTab.add(queries, gbc_queries);

		JPanel panel_4 = new JPanel();
		queries.setLeftComponent(panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 193, 0 };
		gbl_panel_4.rowHeights = new int[] { 17, 72, 0 };
		gbl_panel_4.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		JLabel label_14 = new JLabel("QUERIES");
		label_14.setForeground(Color.BLACK);
		label_14.setFont(new Font("Montserrat", Font.BOLD, 14));
		GridBagConstraints gbc_label_14 = new GridBagConstraints();
		gbc_label_14.anchor = GridBagConstraints.NORTH;
		gbc_label_14.insets = new Insets(0, 0, 5, 0);
		gbc_label_14.gridx = 0;
		gbc_label_14.gridy = 0;
		panel_4.add(label_14, gbc_label_14);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		panel_4.add(scrollPane_1, gbc_scrollPane_1);

		queryList = new JList<String>();
		queryList.setFont(new Font("Montserrat", Font.PLAIN, 11));
		queryList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				try {
					selectQueryID(queryList.getSelectedValue());
				} catch (SEPABindingsException e1) {
					logger.error(e1.getMessage());
				}
			}
		});
		queryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queryList.setModel(queryListDM);
		scrollPane_1.setViewportView(queryList);

		JPanel panel_5 = new JPanel();
		queries.setRightComponent(panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[] { 123, 0 };
		gbl_panel_5.rowHeights = new int[] { 16, 83, 0 };
		gbl_panel_5.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_5.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel_5.setLayout(gbl_panel_5);

		JLabel lblForcedBindings_1 = new JLabel("FORCED BINDINGS");
		lblForcedBindings_1.setFont(new Font("Montserrat", Font.PLAIN, 12));
		lblForcedBindings_1.setForeground(Color.BLACK);
		GridBagConstraints gbc_lblForcedBindings_1 = new GridBagConstraints();
		gbc_lblForcedBindings_1.anchor = GridBagConstraints.NORTH;
		gbc_lblForcedBindings_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblForcedBindings_1.gridx = 0;
		gbc_lblForcedBindings_1.gridy = 0;
		panel_5.add(lblForcedBindings_1, gbc_lblForcedBindings_1);

		JScrollPane scrollPane_3 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridx = 0;
		gbc_scrollPane_3.gridy = 1;
		panel_5.add(scrollPane_3, gbc_scrollPane_3);

		queryForcedBindings = new JTable(subscribeForcedBindingsDM);
		queryForcedBindings.setFont(new Font("Montserrat", Font.PLAIN, 11));
		queryForcedBindings.setFillsViewportHeight(true);
		scrollPane_3.setViewportView(queryForcedBindings);
		queryForcedBindings.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				enableQueryButton();
			}
		});
		queryForcedBindings.setDefaultRenderer(String.class, new ForcedBindingsRenderer());
		queryForcedBindings.registerKeyboardAction(new CopyAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				JComponent.WHEN_FOCUSED);
		queryForcedBindings.setCellSelectionEnabled(true);

		JScrollPane update = new JScrollPane();
		GridBagConstraints gbc_update = new GridBagConstraints();
		gbc_update.fill = GridBagConstraints.BOTH;
		gbc_update.insets = new Insets(0, 0, 5, 5);
		gbc_update.gridx = 0;
		gbc_update.gridy = 2;
		sparqlTab.add(update, gbc_update);

		updateSPARQL = new JTextArea();
		updateSPARQL.setFont(new Font("Montserrat", Font.PLAIN, 13));
		update.setViewportView(updateSPARQL);
		updateSPARQL.setLineWrap(true);

		JScrollPane query = new JScrollPane();
		GridBagConstraints gbc_query = new GridBagConstraints();
		gbc_query.insets = new Insets(0, 0, 5, 0);
		gbc_query.fill = GridBagConstraints.BOTH;
		gbc_query.gridx = 1;
		gbc_query.gridy = 2;
		sparqlTab.add(query, gbc_query);

		querySPARQL = new JTextArea();
		querySPARQL.setFont(new Font("Montserrat", Font.PLAIN, 13));
		querySPARQL.setLineWrap(true);
		query.setViewportView(querySPARQL);

		JPanel panel_6 = new JPanel();
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.insets = new Insets(0, 0, 5, 5);
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 3;
		sparqlTab.add(panel_6, gbc_panel_6);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[] { 0, 137, 0, 55, 45, 34, 0 };
		gbl_panel_6.rowHeights = new int[] { 28, 0 };
		gbl_panel_6.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_6.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_6.setLayout(gbl_panel_6);

		updateButton = new JButton("UPDATE");
		updateButton.setFont(new Font("Montserrat", Font.BOLD, 13));
		GridBagConstraints gbc_updateButton = new GridBagConstraints();
		gbc_updateButton.anchor = GridBagConstraints.WEST;
		gbc_updateButton.insets = new Insets(0, 0, 0, 5);
		gbc_updateButton.gridx = 0;
		gbc_updateButton.gridy = 0;
		panel_6.add(updateButton, gbc_updateButton);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onUpdateButton();
			}
		});
		updateButton.setForeground(Color.BLACK);
		updateButton.setEnabled(false);

		updateInfo = new JLabel("---");
		updateInfo.setFont(new Font("Montserrat", Font.BOLD, 11));
		GridBagConstraints gbc_udpdateInfo = new GridBagConstraints();
		gbc_udpdateInfo.insets = new Insets(0, 0, 0, 5);
		gbc_udpdateInfo.fill = GridBagConstraints.VERTICAL;
		gbc_udpdateInfo.anchor = GridBagConstraints.WEST;
		gbc_udpdateInfo.gridx = 1;
		gbc_udpdateInfo.gridy = 0;
		panel_6.add(updateInfo, gbc_udpdateInfo);

		JLabel lblToms = new JLabel("Timeout (ms)");
		GridBagConstraints gbc_lblToms = new GridBagConstraints();
		gbc_lblToms.insets = new Insets(0, 0, 0, 5);
		gbc_lblToms.gridx = 2;
		gbc_lblToms.gridy = 0;
		panel_6.add(lblToms, gbc_lblToms);
		lblToms.setFont(new Font("Montserrat", Font.PLAIN, 11));
		lblToms.setForeground(Color.BLACK);

		timeout = new JTextField();
		GridBagConstraints gbc_timeout = new GridBagConstraints();
		gbc_timeout.fill = GridBagConstraints.HORIZONTAL;
		gbc_timeout.insets = new Insets(0, 0, 0, 5);
		gbc_timeout.gridx = 3;
		gbc_timeout.gridy = 0;
		panel_6.add(timeout, gbc_timeout);
		timeout.setFont(new Font("Montserrat", Font.PLAIN, 11));
		timeout.setText("5000");
		timeout.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Retry");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 4;
		gbc_lblNewLabel_2.gridy = 0;
		panel_6.add(lblNewLabel_2, gbc_lblNewLabel_2);
		lblNewLabel_2.setFont(new Font("Montserrat", Font.PLAIN, 11));

		nRetry = new JTextField();
		GridBagConstraints gbc_nRetry = new GridBagConstraints();
		gbc_nRetry.fill = GridBagConstraints.HORIZONTAL;
		gbc_nRetry.gridx = 5;
		gbc_nRetry.gridy = 0;
		panel_6.add(nRetry, gbc_nRetry);
		nRetry.setFont(new Font("Montserrat", Font.PLAIN, 11));
		nRetry.setText("0");
		nRetry.setColumns(10);

		JPanel panel_7 = new JPanel();
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.insets = new Insets(0, 0, 5, 0);
		gbc_panel_7.fill = GridBagConstraints.BOTH;
		gbc_panel_7.gridx = 1;
		gbc_panel_7.gridy = 3;
		sparqlTab.add(panel_7, gbc_panel_7);
		GridBagLayout gbl_panel_7 = new GridBagLayout();
		gbl_panel_7.columnWidths = new int[] { 0, 0, 67, 0 };
		gbl_panel_7.rowHeights = new int[] { 0, 0 };
		gbl_panel_7.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_7.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_7.setLayout(gbl_panel_7);

		btnQuery = new JButton("QUERY");
		btnQuery.setFont(new Font("Montserrat", Font.BOLD, 13));
		btnQuery.setEnabled(false);
		btnQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onQueryButton();
			}
		});
		GridBagConstraints gbc_btnQuery = new GridBagConstraints();
		gbc_btnQuery.anchor = GridBagConstraints.WEST;
		gbc_btnQuery.insets = new Insets(0, 0, 0, 5);
		gbc_btnQuery.gridx = 0;
		gbc_btnQuery.gridy = 0;
		panel_7.add(btnQuery, gbc_btnQuery);

		queryInfo = new JLabel("---");
		queryInfo.setFont(new Font("Montserrat", Font.BOLD, 11));
		GridBagConstraints gbc_queryInfo = new GridBagConstraints();
		gbc_queryInfo.fill = GridBagConstraints.VERTICAL;
		gbc_queryInfo.insets = new Insets(0, 0, 0, 5);
		gbc_queryInfo.anchor = GridBagConstraints.WEST;
		gbc_queryInfo.gridx = 1;
		gbc_queryInfo.gridy = 0;
		panel_7.add(queryInfo, gbc_queryInfo);

		subscribeButton = new JButton("SUBSCRIBE");
		subscribeButton.setFont(new Font("Montserrat", Font.BOLD, 13));
		GridBagConstraints gbc_subscribeButton = new GridBagConstraints();
		gbc_subscribeButton.anchor = GridBagConstraints.WEST;
		gbc_subscribeButton.gridx = 2;
		gbc_subscribeButton.gridy = 0;
		panel_7.add(subscribeButton, gbc_subscribeButton);
		subscribeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSubscribeButton();
			}
		});
		subscribeButton.setForeground(Color.BLACK);
		subscribeButton.setEnabled(false);

		JScrollPane results = new JScrollPane();
		GridBagConstraints gbc_results = new GridBagConstraints();
		gbc_results.fill = GridBagConstraints.BOTH;
		gbc_results.gridwidth = 2;
		gbc_results.gridx = 0;
		gbc_results.gridy = 4;
		sparqlTab.add(results, gbc_results);

		bindingsResultsTable = new JTable(bindingsDM);
		bindingsResultsTable.setFont(new Font("Montserrat", Font.PLAIN, 12));
		bindingsResultsTable.setBorder(UIManager.getBorder("Button.border"));
		bindingsResultsTable.setFillsViewportHeight(true);
		bindingsResultsTable.setDefaultRenderer(BindingValue.class, bindingsRender);
		bindingsResultsTable.setAutoCreateRowSorter(true);
		bindingsResultsTable.setCellSelectionEnabled(true);
		bindingsResultsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		bindingsResultsTable.registerKeyboardAction(new CopyAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				JComponent.WHEN_FOCUSED);

		results.setViewportView(bindingsResultsTable);
		bindingsRender.setNamespaces(namespacesDM);

		mainTabs.addTab("Active subscriptions", null, subscriptionsPanel, null);

		JPanel namespaces = new JPanel();
		mainTabs.addTab("Namespaces", null, namespaces, null);
		namespaces.setBorder(null);
		GridBagLayout gbl_namespaces = new GridBagLayout();
		gbl_namespaces.columnWidths = new int[] { 0, 0, 0 };
		gbl_namespaces.rowHeights = new int[] { 43, 0 };
		gbl_namespaces.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_namespaces.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		namespaces.setLayout(gbl_namespaces);

		JScrollPane scrollPane_4 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
		gbc_scrollPane_4.gridwidth = 2;
		gbc_scrollPane_4.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_4.gridx = 0;
		gbc_scrollPane_4.gridy = 0;
		namespaces.add(scrollPane_4, gbc_scrollPane_4);

		namespacesTable = new JTable(namespacesDM);
		namespacesTable.setFont(new Font("Montserrat", Font.PLAIN, 13));
		scrollPane_4.setViewportView(namespacesTable);

		JPanel explorerPanel = new JPanel();
		explorerPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				onExplorerOpenTab(false);
			}
		});
		mainTabs.addTab("Explorer", null, explorerPanel, null);
		GridBagLayout gbl_explorerPanel = new GridBagLayout();
		gbl_explorerPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_explorerPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_explorerPanel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_explorerPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		explorerPanel.setLayout(gbl_explorerPanel);

		graphsEndpointLabel = new JLabel("Endpoint");
		graphsEndpointLabel.setFont(new Font("Montserrat", Font.PLAIN, 13));
		GridBagConstraints gbc_graphsEndpointLabel = new GridBagConstraints();
		gbc_graphsEndpointLabel.insets = new Insets(5, 0, 5, 5);
		gbc_graphsEndpointLabel.gridx = 0;
		gbc_graphsEndpointLabel.gridy = 0;
		explorerPanel.add(graphsEndpointLabel, gbc_graphsEndpointLabel);

		JLabel lblGraphs = new JLabel("Graphs");
		lblGraphs.setFont(new Font("Montserrat", Font.BOLD, 13));
		GridBagConstraints gbc_lblGraphs = new GridBagConstraints();
		gbc_lblGraphs.insets = new Insets(0, 0, 5, 5);
		gbc_lblGraphs.gridx = 0;
		gbc_lblGraphs.gridy = 1;
		explorerPanel.add(lblGraphs, gbc_lblGraphs);

		JButton btnNewButton_1 = new JButton("DROP");
		btnNewButton_1.setFont(new Font("Montserrat", Font.BOLD, 10));
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int[] rows = graphsTable.getSelectedRows();
				if (rows.length > 0) {
					int dialogResult = JOptionPane.showConfirmDialog(null,
							"Are you sure? " + graphs.getValueAt(rows[0], 0).toString() + " will be deleted!",
							"Warning", JOptionPane.YES_NO_OPTION);
					if (dialogResult == JOptionPane.YES_OPTION) {
//						for (int i : rows) {
						Bindings forced = new Bindings();
						forced.addBinding("graph", new RDFTermURI(graphs.getValueAt(rows[0], 0).toString()));
						try {
							if (sepaClient == null) {
								JOptionPane.showMessageDialog(null, "You need to sign in first",
										"Warning: not authorized", JOptionPane.INFORMATION_MESSAGE);
								return;
							}
							sepaClient.update("___DASHBOARD_DROP_GRAPH", forced, Integer.parseInt(timeout.getText()),
									Integer.parseInt(nRetry.getText()));
						} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException
								| SEPABindingsException e1) {
							logger.error(e1.getMessage());
							if (logger.isTraceEnabled())
								e1.printStackTrace();
						}
					}

					onExplorerOpenTab(true);
//					}
				}
			}
		});
		btnNewButton_1.setForeground(Color.RED);
		btnNewButton_1.setBackground(Color.RED);
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(5, 0, 5, 0);
		gbc_btnNewButton_1.gridx = 1;
		gbc_btnNewButton_1.gridy = 1;
		explorerPanel.add(btnNewButton_1, gbc_btnNewButton_1);

		JScrollPane scrollPane_9 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_9 = new GridBagConstraints();
		gbc_scrollPane_9.gridwidth = 2;
		gbc_scrollPane_9.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_9.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_9.gridx = 0;
		gbc_scrollPane_9.gridy = 2;
		explorerPanel.add(scrollPane_9, gbc_scrollPane_9);

		graphsTable = new JTable(graphs);
		graphsTable.setRowSelectionAllowed(false);
		graphsTable.setFont(new Font("Montserrat", Font.PLAIN, 12));
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(graphsTable.getModel());
		Comparator<Integer> compare = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};
		sorter.setComparator(1, compare);
		graphsTable.setRowSorter(sorter);
		graphsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		graphsTable.setCellSelectionEnabled(true);

		// graphsTable.setAutoCreateRowSorter(true);
		scrollPane_9.setViewportView(graphsTable);
		graphsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onExloperSelectGraph();
			}
		});

		JSplitPane splitPane = new JSplitPane();
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.gridwidth = 2;
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 3;
		explorerPanel.add(splitPane, gbc_splitPane);

		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 264, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Class tree");
		lblNewLabel.setFont(new Font("Montserrat", Font.BOLD, 13));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		JScrollPane scrollPane_7 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_7 = new GridBagConstraints();
		gbc_scrollPane_7.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_7.gridx = 0;
		gbc_scrollPane_7.gridy = 1;
		panel.add(scrollPane_7, gbc_scrollPane_7);

		ExplorerTreeRenderer explorerTreeRenderer = new ExplorerTreeRenderer();
		explorerTreeRenderer.setNamespaces(namespacesDM);

		explorerTree = new JTree();
		explorerTree.setFont(new Font("Montserrat", Font.PLAIN, 12));
		scrollPane_7.setViewportView(explorerTree);
		explorerTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				onExplorerSelectTreeElement(e);
			}
		});
		explorerTree.setModel(new ExplorerTreeModel());
		explorerTree.setCellRenderer(explorerTreeRenderer);
		ToolTipManager.sharedInstance().registerComponent(explorerTree);

		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setFont(new Font("Montserrat", Font.BOLD, 13));
		btnRefresh.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onExplorerOpenTab(true);
			}
		});

		JLabel lblNewLabel_1 = new JLabel("Properties");
		lblNewLabel_1.setFont(new Font("Montserrat", Font.BOLD, 13));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 0;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		GridBagConstraints gbc_btnRefresh = new GridBagConstraints();
		gbc_btnRefresh.insets = new Insets(5, 0, 5, 0);
		gbc_btnRefresh.gridx = 2;
		gbc_btnRefresh.gridy = 0;
		panel_1.add(btnRefresh, gbc_btnRefresh);

		currentSubject = new JLabel("uri");
		GridBagConstraints gbc_currentSubject = new GridBagConstraints();
		gbc_currentSubject.gridwidth = 3;
		gbc_currentSubject.insets = new Insets(0, 0, 5, 5);
		gbc_currentSubject.gridx = 0;
		gbc_currentSubject.gridy = 1;
		panel_1.add(currentSubject, gbc_currentSubject);

		buttonStackBackward = new JButton("Back");
		buttonStackBackward.setFont(new Font("Montserrat Alternates", Font.PLAIN, 11));
		GridBagConstraints gbc_buttonStackBackward = new GridBagConstraints();
		gbc_buttonStackBackward.anchor = GridBagConstraints.WEST;
		gbc_buttonStackBackward.insets = new Insets(0, 0, 5, 5);
		gbc_buttonStackBackward.gridx = 0;
		gbc_buttonStackBackward.gridy = 2;
		panel_1.add(buttonStackBackward, gbc_buttonStackBackward);
		buttonStackBackward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onExplorerBackButton(e);
			}
		});
		buttonStackBackward.setVisible(false);

		JLabel lblProperties = new JLabel(
				"Double click on the predicate to follow the URI link or double click on the object to edit it");
		GridBagConstraints gbc_lblProperties = new GridBagConstraints();
		gbc_lblProperties.gridwidth = 4;
		gbc_lblProperties.insets = new Insets(0, 0, 5, 0);
		gbc_lblProperties.gridx = 0;
		gbc_lblProperties.gridy = 3;
		panel_1.add(lblProperties, gbc_lblProperties);
		lblProperties.setFont(new Font("Montserrat", Font.ITALIC, 10));

		JScrollPane scrollPane_8 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_8 = new GridBagConstraints();
		gbc_scrollPane_8.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_8.gridwidth = 3;
		gbc_scrollPane_8.gridx = 0;
		gbc_scrollPane_8.gridy = 4;
		panel_1.add(scrollPane_8, gbc_scrollPane_8);

		tableInstanceProperties = new JTable(tableInstancePropertiesDataModel);
		tableInstanceProperties.setFont(new Font("Montserrat", Font.PLAIN, 12));
		tableInstanceProperties.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onExplorerPropertiesNavigation(e);
			}
		});
		tableInstanceProperties.getModel().addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
				if (e.getFirstRow() == 0)
					return;
				logger.debug("Value: " + tableInstancePropertiesDataModel.getValueAt(e.getFirstRow(), e.getColumn()));

				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) explorerTree
						.getLastSelectedPathComponent();
				Bindings nodeInfo = (Bindings) selectedNode.getUserObject();

				String type = (String) tableInstancePropertiesDataModel.getValueAt(e.getFirstRow(), 2);

				RDFTerm graph = new RDFTermURI((String) graphs
						.getValueAt(graphsTable.convertRowIndexToModel(graphsTable.getSelectedRow()), 0));
				RDFTerm subject = new RDFTermURI(nodeInfo.getValue("instance"));
				RDFTerm predicate = new RDFTermURI(
						(String) tableInstancePropertiesDataModel.getValueAt(e.getFirstRow(), 0));
				RDFTerm object;

				Bindings forcedBindings = new Bindings();
				forcedBindings.addBinding("graph", graph);
				forcedBindings.addBinding("subject", subject);
				forcedBindings.addBinding("predicate", predicate);

				try {
					Response ret = null;

					if (sepaClient == null) {
						JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					if (type == null) {
						object = new RDFTermLiteral(
								(String) tableInstancePropertiesDataModel.getValueAt(e.getFirstRow(), 1));
						forcedBindings.addBinding("object", object);
						ret = sepaClient.update("___DASHBOARD_UPDATE_LITERAL", forcedBindings,
								Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));
					} else if (type.equals("URI") || type.equals("BNODE")) {
						object = new RDFTermURI(
								(String) tableInstancePropertiesDataModel.getValueAt(e.getFirstRow(), 1));
						forcedBindings.addBinding("object", object);
						ret = sepaClient.update("___DASHBOARD_UPDATE_URI", forcedBindings,
								Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));
					} else {
						object = new RDFTermLiteral(
								(String) tableInstancePropertiesDataModel.getValueAt(e.getFirstRow(), 1), type);
						forcedBindings.addBinding("object", object);
						ret = sepaClient.update("___DASHBOARD_UPDATE_LITERAL", forcedBindings,
								Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));
					}

					if (ret.isError())
						logger.error(ret);

				} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException
						| SEPABindingsException e1) {
					logger.error(e1.getMessage());
					if (logger.isTraceEnabled())
						e1.printStackTrace();
				}
			}
		});

		scrollPane_8.setViewportView(tableInstanceProperties);

		JPanel panel_9 = new JPanel();
		GridBagConstraints gbc_panel_9 = new GridBagConstraints();
		gbc_panel_9.anchor = GridBagConstraints.NORTH;
		gbc_panel_9.insets = new Insets(0, 0, 5, 0);
		gbc_panel_9.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_9.gridx = 0;
		gbc_panel_9.gridy = 2;
		frmSepaDashboard.getContentPane().add(panel_9, gbc_panel_9);
		GridBagLayout gbl_panel_9 = new GridBagLayout();
		gbl_panel_9.columnWidths = new int[] { 0, 0, 0, 56, 59, 0, 0 };
		gbl_panel_9.rowHeights = new int[] { 0, 0 };
		gbl_panel_9.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_9.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_9.setLayout(gbl_panel_9);

		JButton btnClean = new JButton("Clear log");
		GridBagConstraints gbc_btnClean = new GridBagConstraints();
		gbc_btnClean.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnClean.insets = new Insets(0, 0, 0, 5);
		gbc_btnClean.gridx = 1;
		gbc_btnClean.gridy = 0;
		panel_9.add(btnClean, gbc_btnClean);
		btnClean.setFont(new Font("Montserrat", Font.BOLD, 13));
		btnClean.setForeground(Color.BLACK);
		btnClean.setBackground(UIManager.getColor("Separator.shadow"));
		btnClean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
				// clear();
			}
		});

		chckbxDatatype = new JCheckBox("Datatype");
		GridBagConstraints gbc_chckbxDatatype = new GridBagConstraints();
		gbc_chckbxDatatype.anchor = GridBagConstraints.EAST;
		gbc_chckbxDatatype.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxDatatype.gridx = 2;
		gbc_chckbxDatatype.gridy = 0;
		panel_9.add(chckbxDatatype, gbc_chckbxDatatype);
		chckbxDatatype.setFont(new Font("Montserrat", Font.PLAIN, 11));
		chckbxDatatype.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onDatatypeCheckbox(e);
			}
		});
		chckbxDatatype.setSelected(true);

		chckbxQname = new JCheckBox("Qname");
		GridBagConstraints gbc_chckbxQname = new GridBagConstraints();
		gbc_chckbxQname.anchor = GridBagConstraints.EAST;
		gbc_chckbxQname.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxQname.gridx = 3;
		gbc_chckbxQname.gridy = 0;
		panel_9.add(chckbxQname, gbc_chckbxQname);
		chckbxQname.setFont(new Font("Montserrat", Font.PLAIN, 11));
		chckbxQname.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onQnameCheckbox(e);
			}
		});
		chckbxQname.setSelected(true);

		JButton btnNewButton_3 = new JButton("CSV");
		GridBagConstraints gbc_btnNewButton_3 = new GridBagConstraints();
		gbc_btnNewButton_3.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnNewButton_3.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_3.gridx = 4;
		gbc_btnNewButton_3.gridy = 0;
		panel_9.add(btnNewButton_3, gbc_btnNewButton_3);
		btnNewButton_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onExportCSV();
			}
		});

		JButton btnNewButton = new JButton("Clear results");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnNewButton.gridx = 5;
		gbc_btnNewButton.gridy = 0;
		panel_9.add(btnNewButton, gbc_btnNewButton);
		btnNewButton.setFont(new Font("Montserrat", Font.BOLD, 13));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});

		scrollPane_5 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_5 = new GridBagConstraints();
		gbc_scrollPane_5.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_5.gridx = 0;
		gbc_scrollPane_5.gridy = 3;
		frmSepaDashboard.getContentPane().add(scrollPane_5, gbc_scrollPane_5);

		textArea = new JTextArea();
		scrollPane_5.setViewportView(textArea);
		textArea.setFont(new Font("Montserrat", Font.PLAIN, 10));
		textArea.setLineWrap(true);
		TextAreaAppender appender = new TextAreaAppender(textArea);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		TextAreaAppender.addAppender(appender, "TextArea");

		JCheckBox chckbxNewCheckBox = new JCheckBox("Hide console");
		chckbxNewCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chckbxNewCheckBox.isSelected()) {
					scrollPane_5.setVisible(false);
					
					Rectangle main = mainTabs.getBounds();
					main.height += textArea.getHeight();
					mainTabs.setBounds(main);
					
					Rectangle control = panel_9.getBounds();
					control.y += textArea.getHeight();
					panel_9.setBounds(control);
					
//					Rectangle top = panel_8.getBounds();
//					Rectangle main = mainTabs.getBounds();
//					Rectangle control = panel_9.getBounds();
//					Rectangle info = scrollPane_5.getBounds();
//					
//					main.height += 20;
//					//control.y = top.height + main.height;
//					//info.y = top.height + main.height + control.height + info.height;
//					info.height = 0;
//					
//					scrollPane_5.setBounds(info);
//					mainTabs.setBounds(main);
					//panel_9.setBounds(control);
					//scrollPane_5.setBounds(info);
					
					//info.y += info.height;
					//scrollPane_5.setBounds(info);
					
					//Rectangle control = panel_9.getBounds();
					//control.y += info.height;
					//panel_9.setBounds(control);
					
										
					
					//Rectangle info = textArea.getBounds();
					
//					int logH = info.height;
//					
//					b.height += logH;
					
					
					//control.y += logH;
					//info.y += logH;
					//mainTabs.setBounds(b);
					//panel_9.setBounds(control);
					//scrollPane_9.setBounds(info);
					//scrollPane_5.setVisible(false);
					
					//info.height = 0;
					//info.y += 10;
					//scrollPane_5.setBounds(info);
					//b.y += 10;
					//scrollPane_9.setBounds(b);
					//scrollPane_5.setVisible(false);
					
					//Rectangle b = panel_9.getBounds();
					//b.y += info.height;
					//panel_9.setBounds(b);
					// Rectangle b = infoPanel.getBounds();
					// b.y += b.height;
					// infoPanel.setBounds(b);
					
				} else {
					scrollPane_5.setVisible(true);
					
					Rectangle main = mainTabs.getBounds();
					main.height -= textArea.getHeight();
					mainTabs.setBounds(main);
					
					Rectangle control = panel_9.getBounds();
					control.y -= textArea.getHeight();
					panel_9.setBounds(control);
					
//					Rectangle info = scrollPane_5.getBounds();
//					info.y -= info.height;
//					//scrollPane_5.setBounds(info);
//					
//					Rectangle control = panel_9.getBounds();
//					control.y -= info.height;
//					panel_9.setBounds(control);
					
					//Rectangle info = textArea.getBounds();
					
//					int logH = info.height;
//					
//					b.height += logH;
					
					
					
					// Rectangle b = infoPanel.getBounds();
					// b.y -= b.height;
					// infoPanel.setBounds(b);
					//scrollPane_5.setVisible(true);
				}
			}
		});
		chckbxNewCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			}
		});

		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 0;
		panel_9.add(chckbxNewCheckBox, gbc_chckbxNewCheckBox);
	}
	
	JScrollPane scrollPane_5;

	protected void onQnameCheckbox(ChangeEvent e) {
		bindingsRender.showAsQName(chckbxQname.isSelected());
		bindingsDM.fireTableDataChanged();
		for (BindingsTableModel table : subscriptionResultsDM.values()) {
			table.fireTableDataChanged();
		}
	}

	protected void onDatatypeCheckbox(ChangeEvent e) {
		bindingsRender.showDataType(chckbxDatatype.isSelected());
		bindingsDM.fireTableDataChanged();
		for (BindingsTableModel table : subscriptionResultsDM.values()) {
			table.fireTableDataChanged();
		}
	}

	protected void onExplorerSelectTreeElement(TreeSelectionEvent e) {
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

				retResponse = sepaClient.query("___DASHBOARD_URI_GRAPH", forced, Integer.parseInt(timeout.getText()),
						Integer.parseInt(nRetry.getText()));
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
	}

	protected void onExplorerBackButton(MouseEvent e) {
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

	protected void onExplorerPropertiesNavigation(MouseEvent e) {
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

	protected void onExloperSelectGraph() {
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

	protected void onExplorerOpenTab(boolean refresh) {
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

	protected void onExportCSV() {
		String openIn = null;
		final JFileChooser fc = new JFileChooser(openIn);
		fc.setDialogTitle("Save CSV");
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		int returnVal = fc.showSaveDialog(frmSepaDashboard);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileName = fc.getSelectedFile().getPath();

			try {
				File fout = new File(fileName);
				FileOutputStream fos = new FileOutputStream(fout);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

				// Header
				String lineString = null;
				for (int col = 0; col < bindingsResultsTable.getColumnCount(); col++) {
					String value = bindingsResultsTable.getColumnName(col);
					if (lineString == null)
						lineString = value;
					else
						lineString += "|" + value;
				}
				bw.write(lineString);
				bw.newLine();

				for (int row = 0; row < bindingsResultsTable.getRowCount(); row++) {
					lineString = null;
					for (int col = 0; col < bindingsResultsTable.getColumnCount(); col++) {
						String value = (((BindingValue) bindingsResultsTable.getValueAt(row, col) == null ? ""
								: ((BindingValue) bindingsResultsTable.getValueAt(row, col)).get()));
						if (lineString == null)
							lineString = value;
						else
							lineString += "|" + value;
					}
					bw.write(lineString);
					bw.newLine();

				}
				bw.close();
			} catch (Exception e3) {
				logger.error(e3.getMessage());
			}
		}
	}

	protected boolean onLoadJSAPButton() throws SEPASecurityException, URISyntaxException {
		String openIn = null;
		if (appProperties.getProperty("appProfile") != null) {
			String[] profilePath = appProperties.getProperty("appProfile").split(",");
			openIn = profilePath[profilePath.length - 1];
		}

		final JFileChooser fc = new JFileChooser(openIn);
		fc.setDialogTitle("Open JSAP file");
		DashboardFileFilter filter = new DashboardFileFilter("JSON SAP Profile (.jsap)", ".jsap");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(frmSepaDashboard);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileName = fc.getSelectedFile().getPath();

			if (jsapFiles.contains(fileName)) {
				jsapFiles.remove(fileName);
			}

			if (loadSAP(fileName, !chckbxMerge.isSelected())) {
				String path = "";
				for (int i = 0; i < jsapFiles.size(); i++) {
					if (i == 0)
						path = jsapFiles.get(i);
					else
						path = path + "," + jsapFiles.get(i);
				}

				appProperties = new Properties();
				appProperties.put("appProfile", path);

				logger.info("JSAP files: " + path);

				return storeDashboardProperties();
			}
		}

		return false;
	}

	private boolean storeDashboardProperties() {
		try {
			FileOutputStream out = new FileOutputStream("dashboard.properties");
			appProperties.store(out, "Dashboard properties");
			out.close();
		} catch (IOException e3) {
			logger.error(e3.getMessage());
			return false;
		}

		return true;
	}

	protected void onSubscribeButton() {
		try {
			subscribe();
		} catch (IOException | SEPAPropertiesException | NumberFormatException | SEPAProtocolException
				| SEPASecurityException | SEPABindingsException | InterruptedException e1) {
			logger.error(e1.getMessage());
		}
	}

	protected void onQueryButton() {
		try {
			query();
		} catch (SEPAPropertiesException | SEPABindingsException e1) {
			logger.error(e1.getMessage());
		}
	}

	protected void onUpdateButton() {
		try {
			update();
		} catch (SEPAPropertiesException | SEPABindingsException e1) {
			logger.error(e1.getMessage());
		}
	}

	protected void clear() {
		if (sparqlTab.isShowing()) {
			bindingsDM.clear();
		} else {
			for (String spuid : subscriptionResultsTables.keySet()) {
				if (subscriptionResultsTables.get(spuid).isShowing()) {
					subscriptionResultsDM.get(spuid).clear();
					subscriptionResultsLabels.get(spuid).setText("Results cleaned");
				}
			}
		}
	}

	protected void subscribe() throws IOException, SEPAPropertiesException, NumberFormatException,
			SEPAProtocolException, SEPASecurityException, SEPABindingsException, InterruptedException {
//		subOp = SUB_OP.SUB;

		Bindings bindings = new Bindings();
		for (int row = 0; row < queryForcedBindings.getRowCount(); row++) {
			String type;
			if (queryForcedBindings.getValueAt(row, 2) == null)
				type = "xsd:string";
			else
				type = queryForcedBindings.getValueAt(row, 2).toString();
			String value = queryForcedBindings.getValueAt(row, 1).toString();
			String variable = queryForcedBindings.getValueAt(row, 0).toString();
			if (type.toUpperCase().equals("URI"))
				bindings.addBinding(variable, new RDFTermURI(value));
			else if (type.toUpperCase().equals("BNODE"))
				bindings.addBinding(variable, new RDFTermBNode(value));
			else
				bindings.addBinding(variable, new RDFTermLiteral(value, type));
		}

		if (sepaClient == null) {
			JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		sepaClient.subscribe(queryID, querySPARQL.getText(), bindings, Integer.parseInt(timeout.getText()),
				Integer.parseInt(nRetry.getText()));
	}

	protected void query() throws SEPAPropertiesException, SEPABindingsException {
		Bindings bindings = new Bindings();
		for (int row = 0; row < queryForcedBindings.getRowCount(); row++) {
			String type = null;
			if (queryForcedBindings.getValueAt(row, 2) != null)
				type = queryForcedBindings.getValueAt(row, 2).toString();
			String value = queryForcedBindings.getValueAt(row, 1).toString();
			String variable = queryForcedBindings.getValueAt(row, 0).toString();

			if (type == null)
				bindings.addBinding(variable, new RDFTermLiteral(value));
			else if (type.toUpperCase().equals("URI"))
				bindings.addBinding(variable, new RDFTermURI(value));
			else if (type.toUpperCase().equals("BNODE"))
				bindings.addBinding(variable, new RDFTermBNode(value));
			else
				bindings.addBinding(variable, new RDFTermLiteral(value, type));
		}

		try {
			Instant start = Instant.now();

			if (sepaClient == null) {
				JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			Response ret = sepaClient.query(queryID, querySPARQL.getText(), bindings,
					Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));
			Instant stop = Instant.now();
			if (ret.isError()) {
				logger.error(ret.toString() + String.format(" (%d ms)", (stop.toEpochMilli() - start.toEpochMilli())));
				queryInfo.setText("Error: " + ((ErrorResponse) ret).getStatusCode());

				// Security
				ErrorResponse error = (ErrorResponse) ret;
				if (error.isTokenExpiredError()) {
					sm.refreshToken();
					query();
				}

			} else {
				QueryResponse results = (QueryResponse) ret;
				logger.info(String.format("Results: %d (%d ms)", results.getBindingsResults().size(),
						(stop.toEpochMilli() - start.toEpochMilli())));
				queryInfo.setText(String.format("Results: %d (%d ms)", results.getBindingsResults().size(),
						(stop.toEpochMilli() - start.toEpochMilli())));
				bindingsDM.clear();
				bindingsDM.setAddedResults(results.getBindingsResults(), null);
			}
		} catch (NumberFormatException | SEPAProtocolException | SEPASecurityException | IOException e) {
			logger.error(e.getMessage());
		}
	}

	protected void update() throws SEPAPropertiesException, SEPABindingsException {
		Bindings bindings = new Bindings();
		for (int row = 0; row < updateForcedBindings.getRowCount(); row++) {
			String type;
			if (updateForcedBindings.getValueAt(row, 2) == null)
				type = null;
			else
				type = updateForcedBindings.getValueAt(row, 2).toString();
			String value = updateForcedBindings.getValueAt(row, 1).toString();
			String variable = updateForcedBindings.getValueAt(row, 0).toString();

			if (type == null)
				bindings.addBinding(variable, new RDFTermLiteral(value));
			else if (type.toUpperCase().equals("URI"))
				bindings.addBinding(variable, new RDFTermURI(value));
			else if (type.toUpperCase().equals("BNODE"))
				bindings.addBinding(variable, new RDFTermBNode(value));
			else
				bindings.addBinding(variable, new RDFTermLiteral(value, type));
		}

		try {
			Instant start = Instant.now();

			if (sepaClient == null) {
				JOptionPane.showMessageDialog(null, "You need to sign in first", "Warning: not authorized",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			Response ret = sepaClient.update(updateID, updateSPARQL.getText(), bindings,
					Integer.parseInt(timeout.getText()), Integer.parseInt(nRetry.getText()));
			Instant stop = Instant.now();
			if (ret.isError()) {
				logger.error(ret.toString() + String.format(" (%d ms)", (stop.toEpochMilli() - start.toEpochMilli())));
				updateInfo.setText("Error: " + ((ErrorResponse) ret).getStatusCode());

				// Security
				ErrorResponse error = (ErrorResponse) ret;
				if (error.isTokenExpiredError()) {
					sm.refreshToken();
					update();
				}
			} else {
				logger.info(String.format("Update OK (%d ms)", (stop.toEpochMilli() - start.toEpochMilli())));
				updateInfo.setText(String.format("Update OK (%d ms)", (stop.toEpochMilli() - start.toEpochMilli())));
			}
		} catch (NumberFormatException | SEPAProtocolException | SEPASecurityException | IOException e) {
			logger.error(e.getMessage());
		}
	}

	protected void selectUpdateID(String id) throws SEPABindingsException {
		if (id == null)
			return;
		updateID = id;
		// JSAP app = sepaClient.getApplicationProfile();
		updateSPARQL.setText(appProfile.getSPARQLUpdate(id));

		Bindings bindings = appProfile.getUpdateBindings(id);
		updateForcedBindingsDM.clearBindings();
		for (String variable : bindings.getVariables()) {
			if (bindings.isURI(variable))
				updateForcedBindingsDM.addBindings(variable, "URI", bindings.getValue(variable));
			else if (bindings.isLiteral(variable))
				updateForcedBindingsDM.addBindings(variable, bindings.getDatatype(variable),
						bindings.getValue(variable));
			else
				updateForcedBindingsDM.addBindings(variable, "BNODE", bindings.getValue(variable));
		}

		String port = "";
		if (appProfile.getUpdatePort(id) != -1)
			port = ":" + appProfile.getUpdatePort(id);
		String url = appProfile.getUpdateProtocolScheme(id) + "://" + appProfile.getUpdateHost(id) + port
				+ appProfile.getUpdatePath(id);
		if (appProfile.getUpdateMethod(id).equals(UpdateHTTPMethod.POST))
			updateURL.setText("POST " + url);
		else if (appProfile.getUpdateMethod(id).equals(UpdateHTTPMethod.URL_ENCODED_POST))
			updateURL.setText("URL ENCODED POST " + url);

		usingGraphURI.setText(appProfile.getUsingGraphURI(id).toString());
		usingNamedGraphURI.setText(appProfile.getUsingNamedGraphURI(id).toString());

		enableUpdateButton();
	}

	protected void selectQueryID(String id) throws SEPABindingsException {
		if (id == null)
			return;

		queryID = id;
		// JSAP app = sepaClient.getApplicationProfile();
		querySPARQL.setText(appProfile.getSPARQLQuery(id));

		Bindings bindings = appProfile.getQueryBindings(id);
		subscribeForcedBindingsDM.clearBindings();
		for (String variable : bindings.getVariables()) {
			if (bindings.isURI(variable))
				subscribeForcedBindingsDM.addBindings(variable, "URI", bindings.getValue(variable));
			else if (bindings.isLiteral(variable))
				subscribeForcedBindingsDM.addBindings(variable, bindings.getDatatype(variable),
						bindings.getValue(variable));
			else
				subscribeForcedBindingsDM.addBindings(variable, "BNODE", bindings.getValue(variable));
		}

		String port = "";
		if (appProfile.getQueryPort(id) != -1)
			port = ":" + appProfile.getQueryPort(id);
		String url = appProfile.getQueryProtocolScheme(id) + "://" + appProfile.getQueryHost(id) + port
				+ appProfile.getQueryPath(id);

		if (appProfile.getQueryMethod(id).equals(QueryHTTPMethod.GET))
			queryURL.setText("GET " + url);
		else if (appProfile.getQueryMethod(id).equals(QueryHTTPMethod.POST))
			queryURL.setText("POST " + url);
		else if (appProfile.getQueryMethod(id).equals(QueryHTTPMethod.URL_ENCODED_POST))
			queryURL.setText("URL ENCODED POST " + url);

		if (appProfile.getSubscribeProtocol(id).equals(SubscriptionProtocol.WS))
			url = "ws://";
		else if (appProfile.getSubscribeProtocol(id).equals(SubscriptionProtocol.WSS))
			url = "wss://";
		url += appProfile.getSubscribeHost(id);
		if (appProfile.getSubscribePort(id) != -1)
			url += ":" + appProfile.getSubscribePort(id);
		url += appProfile.getSubscribePath(id);
		subscribeURL.setText(url);

		defaultGraphURI.setText(appProfile.getDefaultGraphURI(id).toString());
		namedGraphURI.setText(appProfile.getNamedGraphURI(id).toString());

		enableQueryButton();
	}

	protected boolean checkType(String value, String type) {
		if (type == null)
			return true;

		try {
			switch (type) {
			case "URI":
				if (value.equals(""))
					return false;
				URI check = new URI(value);
				if (check.getScheme() == null)
					return false;
				break;
			case "xsd:base64Binary":
				Integer.parseInt(value, 16);
				break;
			case "xsd:boolean":
				if (!(value.equals("true") || value.equals("false") || value.equals("0") || value.equals("1")))
					return false;
				break;
			case "xsd:byte":
				Byte.parseByte(value);
				break;
			case "xsd:date":
			case "xsd:dateTime":
			case "xsd:time":
				DatatypeFactory.newInstance().newXMLGregorianCalendar(value);
				break;
			case "xsd:decimal":
				new java.math.BigDecimal(value);
				break;
			case "xsd:double":
				Double.parseDouble(value);
				break;
			case "xsd:float":
				Float.parseFloat(value);
				break;
			case "xsd:int":
				Integer.parseInt(value);
				break;
			case "xsd:integer":
				new java.math.BigInteger(value);
				break;
			case "xsd:long":
				Long.parseLong(value);
				break;
			case "xsd:short":
				Short.parseShort(value);
				break;
			case "xsd:QName":
				new javax.xml.namespace.QName(value);
				break;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	protected void enableUpdateButton() {
		updateButton.setEnabled(false);
		if (updateSPARQL.getText().equals(""))
			return;
		else {
			for (int row = 0; row < updateForcedBindings.getRowCount(); row++) {
				String type = null;
				if (updateForcedBindings.getValueAt(row, 2) != null)
					type = updateForcedBindings.getValueAt(row, 2).toString();
				String value = updateForcedBindings.getValueAt(row, 1).toString();
				if (!checkType(value, type))
					return;
			}
		}
		updateButton.setEnabled(true);
	}

	protected void enableQueryButton() {
		btnQuery.setEnabled(false);
		subscribeButton.setEnabled(false);
		if (querySPARQL.getText().equals(""))
			return;
		else {
			for (int row = 0; row < queryForcedBindings.getRowCount(); row++) {
				String type = null;

				if (queryForcedBindings.getValueAt(row, 2) != null)
					type = queryForcedBindings.getValueAt(row, 2).toString();
				String value = queryForcedBindings.getValueAt(row, 1).toString();
				if (!checkType(value, type))
					return;
			}
		}
		btnQuery.setEnabled(true);
		subscribeButton.setEnabled(true);
	}

	// LOGIN

	@Override
	public void onLogin(String id) {// , String secret,boolean remember) {
		try {
			sepaClient = new GenericClient(appProfile, handler);

			login.setVisible(false);
			btnLogin.setEnabled(true);
			btnLogin.setText("Sign out");
			signedIn = true;

			frmSepaDashboard.setTitle(title + " - Client ID: " + id);

		} catch (SEPAProtocolException | SEPASecurityException | SEPAPropertiesException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void onLoginClose() {

	}

	@Override
	public void onLoginError(ErrorResponse err) {
		logger.error(err);
	}
}
