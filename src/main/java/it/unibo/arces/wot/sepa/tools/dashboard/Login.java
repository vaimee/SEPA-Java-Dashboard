package it.unibo.arces.wot.sepa.tools.dashboard;

import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.RegistrationResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.security.ClientSecurityManager;

import java.awt.GridBagLayout;
import javax.swing.JRadioButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Login extends JDialog implements ActionListener {
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 544263217213326603L;
	private final JPanel contentPanel = new JPanel();
	private JTextField ID;
	private JPasswordField PWD;
	private JLabel lblPassword;

	private JRadioButton rdbtnSignIn;
	private JRadioButton rdbtnRegisterANew;
	
	private JButton btnLogin;

	/**
	 * Create the dialog.
	 */
	public Login(ClientSecurityManager sm,LoginListener m_listener,JFrame parent) {
		setType(Type.POPUP);
		setModal(true);
		if (sm == null) throw new IllegalArgumentException("Security manager is null");
		if (m_listener == null) throw new IllegalArgumentException("Listener is null");
		
		ButtonGroup group = new ButtonGroup();
		
		setResizable(false);
		setLocationRelativeTo(parent);
		
		// add a window listener
	    addWindowListener(new WindowAdapter()
	    {
	      public void windowClosed(WindowEvent e)
	      {
	        logger.info("jdialog window closed");
	      }

	      public void windowClosing(WindowEvent e)
	      {
	    	  logger.info("jdialog window closing");
	    	  m_listener.onLoginClose();
	      }
	    });
	    
		setTitle("LOGIN");
		setBounds(100, 100, 347, 183);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{97, 75, 175, 0};
		gbl_contentPanel.rowHeights = new int[]{23, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{		
			{
				rdbtnRegisterANew = new JRadioButton("Register a new identity");
				GridBagConstraints gbc_rdbtnRegisterANew = new GridBagConstraints();
				gbc_rdbtnRegisterANew.gridwidth = 3;
				gbc_rdbtnRegisterANew.insets = new Insets(0, 0, 5, 5);
				gbc_rdbtnRegisterANew.anchor = GridBagConstraints.NORTHWEST;
				gbc_rdbtnRegisterANew.gridx = 0;
				gbc_rdbtnRegisterANew.gridy = 0;
				contentPanel.add(rdbtnRegisterANew, gbc_rdbtnRegisterANew);
			}
			group.add(rdbtnRegisterANew);
			rdbtnRegisterANew.addActionListener(this);
			
			
			{
				rdbtnSignIn = new JRadioButton("Sign in");
				rdbtnSignIn.setSelected(true);
				GridBagConstraints gbc_rdbtnSignIn = new GridBagConstraints();
				gbc_rdbtnSignIn.gridwidth = 3;
				gbc_rdbtnSignIn.anchor = GridBagConstraints.NORTHWEST;
				gbc_rdbtnSignIn.insets = new Insets(0, 0, 5, 5);
				gbc_rdbtnSignIn.gridx = 0;
				gbc_rdbtnSignIn.gridy = 1;
				contentPanel.add(rdbtnSignIn, gbc_rdbtnSignIn);
			}
			group.add(rdbtnSignIn);
			rdbtnSignIn.addActionListener(this);
			{
				JLabel lblUsername = new JLabel("ID");
				GridBagConstraints gbc_lblUsername = new GridBagConstraints();
				gbc_lblUsername.anchor = GridBagConstraints.EAST;
				gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
				gbc_lblUsername.gridx = 0;
				gbc_lblUsername.gridy = 2;
				contentPanel.add(lblUsername, gbc_lblUsername);
			}
			{
				ID = new JTextField();
				GridBagConstraints gbc_ID = new GridBagConstraints();
				gbc_ID.gridwidth = 2;
				gbc_ID.insets = new Insets(0, 0, 5, 5);
				gbc_ID.fill = GridBagConstraints.HORIZONTAL;
				gbc_ID.gridx = 1;
				gbc_ID.gridy = 2;
				contentPanel.add(ID, gbc_ID);
				ID.setColumns(10);
			}
			{
				lblPassword = new JLabel("Password");
				GridBagConstraints gbc_lblPassword = new GridBagConstraints();
				gbc_lblPassword.anchor = GridBagConstraints.EAST;
				gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
				gbc_lblPassword.gridx = 0;
				gbc_lblPassword.gridy = 3;
				contentPanel.add(lblPassword, gbc_lblPassword);
			}
			{
				PWD = new JPasswordField();
				GridBagConstraints gbc_PWD = new GridBagConstraints();
				gbc_PWD.gridwidth = 2;
				gbc_PWD.insets = new Insets(0, 0, 5, 5);
				gbc_PWD.fill = GridBagConstraints.HORIZONTAL;
				gbc_PWD.gridx = 1;
				gbc_PWD.gridy = 3;
				contentPanel.add(PWD, gbc_PWD);
			}
		}
		btnLogin = new JButton("Login");
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (rdbtnSignIn.isSelected()) {
					try {
						sm.setClientCredentials(ID.getText(), new String(PWD.getPassword()));
						
						Response ret = sm.refreshToken();
						if (ret.isError()) {
							logger.error(ret);
							m_listener.onLoginError((ErrorResponse)ret);
							setTitle("Wrong credentials");
							return;
						}
						
						sm.storeOAuthProperties();
						
						m_listener.onLogin(ID.getText());
					} catch (SEPAPropertiesException | SEPASecurityException e1) {
						logger.error(e1.getMessage());
						m_listener.onLoginError(new ErrorResponse(401,"not_authorized",e1.getMessage()));
					}
				}
				else {
					try {
						Response ret = sm.register(ID.getText());
						
						if (ret.isError()) {
							logger.error(ret);
							m_listener.onLoginError((ErrorResponse)ret);
							return;	
						}
						
						RegistrationResponse reg = (RegistrationResponse) ret;
						
						if (reg.isError()) {
							setTitle("Failed to register");
						}
						else {
							ID.setText(reg.getClientId());
							PWD.setText(reg.getClientSecret());
							
//							ID.setVisible(true);
//							PWD.setVisible(true);
							
							rdbtnSignIn.setSelected(true);
							changeSelection();
					
							m_listener.onRegister();	
						}

						
					} catch (SEPASecurityException | SEPAPropertiesException e1) {
						logger.error(e1.getMessage());
						m_listener.onLoginError(new ErrorResponse(401,"not_authorized",e1.getMessage()));
					}
				}
			}
		});
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.anchor = GridBagConstraints.EAST;
		gbc_btnLogin.gridx = 2;
		gbc_btnLogin.gridy = 4;
		contentPanel.add(btnLogin, gbc_btnLogin);
		
		if(sm.isClientRegistered()) {
			ID.setText(sm.getClientId());
		}
	}
	
	private void changeSelection() {
		if (rdbtnSignIn.isSelected()) {
			btnLogin.setText("Login");
			ID.setEditable(true);
			ID.setEnabled(true);
			
			lblPassword.setVisible(true);
			PWD.setVisible(true);
			PWD.setEditable(true);
			PWD.setEnabled(true);
		}
		else {
			btnLogin.setText("Register");
			lblPassword.setVisible(false);
			PWD.setVisible(false);
		}	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		changeSelection();		
	}
}
