package it.unibo.arces.wot.sepa.tools.dashboard;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.api.ISubscriptionHandler;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.Notification;
import it.unibo.arces.wot.sepa.commons.sparql.ARBindingsResults;
import it.unibo.arces.wot.sepa.tools.dashboard.bindings.BindingsRender;
import it.unibo.arces.wot.sepa.tools.dashboard.tableModels.BindingsTableModel;
import it.unibo.arces.wot.sepa.tools.dashboard.utils.CopyAction;

public class DashboardHandler implements ISubscriptionHandler {
	protected String unsubSpuid;

	private static final Logger logger = LogManager.getLogger();

	private HashMap<String, JPanel> subscriptions = new HashMap<String, JPanel>();

	private HashMap<String, BindingsTableModel> subscriptionResultsDM = new HashMap<String, BindingsTableModel>();
	private HashMap<String, JLabel> subscriptionResultsLabels = new HashMap<String, JLabel>();
	private HashMap<String, JTable> subscriptionResultsTables = new HashMap<String, JTable>();
	private JList<String> queryList;
	private JTextArea querySPARQL;
	private DashboadApp sepaClient;
	private JTextField timeout;
	private JTextField nRetry;
	private BindingsRender bindingsRender;
	private JTabbedPane subscriptionsPanel;
	private JTabbedPane mainTabs;
	
	public DashboardHandler(HashMap<String, BindingsTableModel> subscriptionResultsDM,
			HashMap<String, JLabel> subscriptionResultsLabels, HashMap<String, JTable> subscriptionResultsTables,
			JList<String> queryList, JTextArea querySPARQL, JTextField timeout, JTextField nRetry,BindingsRender bindingsRender,JTabbedPane subscriptionsPanel, JTabbedPane mainTabs) {
		this.subscriptionResultsDM = subscriptionResultsDM;
		this.subscriptionResultsLabels = subscriptionResultsLabels;
		this.subscriptionResultsTables = subscriptionResultsTables;
		this.queryList = queryList;
		this.querySPARQL = querySPARQL;
		this.timeout = timeout;
		this.nRetry = nRetry;
		this.bindingsRender = bindingsRender;
		this.subscriptionsPanel= subscriptionsPanel;
		this.mainTabs= mainTabs; 
	}

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
				subscriptionResultsDM.get(spuid).setResults(subscriptionResultsTables, notify, spuid);
			} catch (SEPABindingsException e) {
				logger.error(e.getMessage());
			}

			subscriptionResultsLabels.get(spuid)
					.setText("Bindings results (" + subscriptionResultsDM.get(spuid).getRowCount() + ") Added(" + added
							+ ") + Removed (" + removed + ")");
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
//					subOp = SUB_OP.UNSUB;
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
				} catch (NumberFormatException | SEPASecurityException | SEPAPropertiesException | SEPAProtocolException
						| InterruptedException | SEPABindingsException e1) {
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
				KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
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
	
	public void setSepaClient(DashboadApp sepaClient) {
		this.sepaClient = sepaClient;
	}
}
