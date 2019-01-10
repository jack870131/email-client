import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class EmailClient extends Frame {
    /* The stuff for the GUI. */
    private Button btSend = new Button("Send");
    private Button btClear = new Button("Clear");
    private Button btQuit = new Button("Quit");
    private Label serverLabel = new Label("Local SMTP server:");
    private TextField serverField = new TextField("student.csc.liv.ac.uk", 40);
    private Label serverPortLabel = new Label("Port number of SMTP server:");
    private TextField serverPortField = new TextField("1025", 30);
    private Label fromLabel = new Label("From:");
    private TextField fromField = new TextField(getSenderAddress(), 40);
    private Label toLabel = new Label("To:");
    private TextField toField = new TextField("test1@test.com", 40);
    private Label ccLabel = new Label("Cc:");
    private TextField ccField = new TextField("test2@test.com test3@test.com", 40);
    private Label subjectLabel = new Label("Subject:");
    private TextField subjectField = new TextField("Hello", 40);
    private Label messageLabel = new Label("Message:");
    private TextArea messageText = new TextArea("How are you?", 30, 80);
    private Label urlLabel = new Label("HTTP://");
    private TextField urlField = new TextField("cgi.csc.liv.ac.uk/~gairing/test.txt", 40);
    private Button btGet = new Button("Get");

    /**
     * Create a new EmailClient window with fields for entering all
     * the relevant information (From, To, Subject, and message).
     */
    public EmailClient() {
        super("Java Emailclient");
	
	/* Create panels for holding the fields. To make it look nice,
	   create an extra panel for holding all the child panels. */
        Panel serverPanel = new Panel(new BorderLayout());
        Panel serverPortPanel = new Panel(new BorderLayout());
        Panel fromPanel = new Panel(new BorderLayout());
        Panel toPanel = new Panel(new BorderLayout());
        Panel ccPanel = new Panel(new BorderLayout());
        Panel subjectPanel = new Panel(new BorderLayout());
        Panel messagePanel = new Panel(new BorderLayout());
        serverPanel.add(serverLabel, BorderLayout.WEST);
        serverPanel.add(serverField, BorderLayout.CENTER);
        serverPortPanel.add(serverPortLabel, BorderLayout.WEST);
        serverPortPanel.add(serverPortField, BorderLayout.CENTER);
        fromPanel.add(fromLabel, BorderLayout.WEST);
        fromPanel.add(fromField, BorderLayout.CENTER);
        toPanel.add(toLabel, BorderLayout.WEST);
        toPanel.add(toField, BorderLayout.CENTER);
        ccPanel.add(ccLabel, BorderLayout.WEST);
        ccPanel.add(ccField, BorderLayout.CENTER);
        subjectPanel.add(subjectLabel, BorderLayout.WEST);
        subjectPanel.add(subjectField, BorderLayout.CENTER);
        messagePanel.add(messageLabel, BorderLayout.NORTH);
        messagePanel.add(messageText, BorderLayout.CENTER);
        Panel fieldPanel = new Panel(new GridLayout(0, 1));
        fieldPanel.add(serverPanel);
        fieldPanel.add(serverPortPanel);
        fieldPanel.add(fromPanel);
        fieldPanel.add(toPanel);
        fieldPanel.add(ccPanel);
        fieldPanel.add(subjectPanel);
		
	/* Create a panel for the URL field and add listener to the GET 
	   button. */
        Panel urlPanel = new Panel(new BorderLayout());
        urlPanel.add(urlLabel, BorderLayout.WEST);
        urlPanel.add(urlField, BorderLayout.CENTER);
        urlPanel.add(btGet, BorderLayout.EAST);
        fieldPanel.add(urlPanel);
        btGet.addActionListener(new GetListener());
		

	/* Create a panel for the buttons and add listeners to the
	   buttons. */
        Panel buttonPanel = new Panel(new GridLayout(1, 0));
        btSend.addActionListener(new SendListener());
        btClear.addActionListener(new ClearListener());
        btQuit.addActionListener(new QuitListener());
        buttonPanel.add(btSend);
        buttonPanel.add(btClear);
        buttonPanel.add(btQuit);

        /* Add, pack, and show. */
        add(fieldPanel, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    /* Construct the sender address instead of using the user-supplied value */
    public String getSenderAddress() {
        InetAddress inetAddress = null;
        String hostName = null;
        String userName = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            // The name of the local host
            hostName = inetAddress.getHostName();
            // Get the currently logged in username
            userName = System.getProperty("user.name");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return userName + "@" + hostName;
    }

    static public void main(String argv[]) {
        new EmailClient();
    }

    /* Handler for the Send-button. */
    class SendListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            System.out.println("Sending mail");

            /* Check that we have the local mailserver */
            if ((serverField.getText()).equals("")) {
                System.out.println("Need name of SMTP server!");
                return;
            }

            /* Check that we have a port number */
            if ((serverPortField.getText()).equals("")) {
                System.out.println("Need Port number of  SMTP server!");
                return;
            }


            /* Check that we have the sender and recipient. */
            if ((fromField.getText()).equals("")) {
                System.out.println("Need sender!");
                return;
            }
            if ((toField.getText()).equals("")) {
                System.out.println("Need recipient!");
                return;
            }

            /* Create the message */
            EmailMessage mailMessage;
            try {
                mailMessage = new EmailMessage(fromField.getText(),
                        getRecipients(),
                        subjectField.getText(),
                        messageText.getText(),
                        serverField.getText(),
                        Integer.parseInt(serverPortField.getText()));
            } catch (UnknownHostException e) {
                /* If there is an error, do not go further */
                return;
            }
        
	    /* Check that the message is valid, i.e., sender and
	       recipient addresses look ok. */
            if (!mailMessage.isValid()) {
                return;
            }

            try {
                SMTPConnect connection = new SMTPConnect(mailMessage);
                connection.send(mailMessage);
                connection.close();
            } catch (IOException error) {
                System.out.println("Sending failed: " + error);
                return;
            }
            System.out.println("Email sent succesfully!");
        }

        /* Add multiple recipients and put them into an array */
        public String[] getRecipients() {
            String[] recipients = (toField.getText().trim() + " " +
                    ccField.getText().trim()).split(" ");
            return recipients;
        }
    }

    /* Get URL if specified. */
    class GetListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String receivedText;

            /* Check if URL field is empty. */
            if ((urlField.getText()).equals("")) {
                System.out.println("Need URL!");
                return;
            }
			/* Pass string from URL field to HTTPGet (trimmed);
			   returned string is either requested object 
			   or some error message. */
            HttpInteract request = new HttpInteract(urlField.getText().trim());

            // Send http request. Returned String holds object
            try {
                receivedText = request.send();
            } catch (IOException error) {
                messageText.setText("Downloading File failed.\r\nIOException: " + error);
                return;
            }
            // Change message text
            urlField.setText(request.getURL());
            messageText.setText(receivedText);
        }
    }


    /* Clear the fields on the GUI. */
    class ClearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("Clearing fields");
            fromField.setText("");
            toField.setText("");
            subjectField.setText("");
            messageText.setText("");
        }
    }

    /* Quit. */
    class QuitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}

