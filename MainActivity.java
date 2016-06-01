package com.kaanaxinc.portalstudio;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {
    // La chaîne de caractères par défaut
    private final String defaut = "Le code d'accès est inscrit sur la notice.";
    Toast myToast;
    Button sendPost = null;
    Button sendComment = null;
    ImageButton logo = null;
    TextView entrezCode = null;
    TextView codeSurNotice = null;
    TextView texteRecu = null;
    Handler bluetoothIn;
    boolean currentView = false;
    final int handlerState = 0;
    String messageFinal = "jmesoiejsmoi";

    BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
    private OutputStream outputStream;
    private InputStream inStream;
    ArrayList<String> postIds = new ArrayList<String>();
    int postNumber = 0;
    String subreddit = "";

    String[] subreddits = {"talesfromtechsupport", "talesfromretail", "caddit", "askreddit", "nosleep"};
    char[] subCommands = {'t', 'r', 'c', 'a', 'n'};
    String messageAEnvoyer = "";
    int progressionEnvoi = 0;
    int progressionPage = 0;

    String connectionError = "PortalStudio n'a pas pu se connecter au portail. Vérifiez votre connexion bluetooth. Tapez sur le logo pour réessayer la connexion.";

    //Initializes the Bluetooth connection.
    //Important detail: it always connects to the device that is the lowest in your paired devices list.
    private void init() throws IOException {
        Thread connexionThread = new Thread(new Runnable()
        {
            public void run() {
                try {
                    if (blueAdapter != null) {
                        if (blueAdapter.isEnabled()) {
                            Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                            if(bondedDevices.size() > 0){
                                BluetoothDevice bt2 = null;
                                for(BluetoothDevice bt : bondedDevices) {
                                    bt2 = bt;
                                }


                                BluetoothDevice device = bt2;
                                ParcelUuid[] uuids = device.getUuids();
                                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                                socket.connect();
                                outputStream = socket.getOutputStream();
                                inStream = socket.getInputStream();
                            }

                            Log.e("error", "No appropriate paired devices.");
                        } else {
                            Log.e("error", "Bluetooth is disabled.");
                        }
                    }
                } catch (IOException e) {

                }
            }
        });

        connexionThread.start();

    }

    //Writes a string to the bluetooth device.
    public void write(String s) throws IOException, NullPointerException {
        outputStream.write(s.getBytes());
    }

    //Once this method is called, begins listening for data.
    //Note: always place this method in a if checking if inStream != null. Else it will crash.
    void beginListenForData() {
        Thread workerThread = new Thread(new Runnable()
        {
            public void run() {

                byte[] buffer = new byte[256];
                int bytes;

                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        bytes = inStream.read(buffer);            //read bytes from input buffer
                        String readMessage = new String(buffer, 0, bytes);
                        // Send the obtained bytes to the UI Activity via handler
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    } catch (IOException e) {
                        break;
                    } catch (NullPointerException e) {
                        toast("Null pointer exception 3");
                        break;
                    }
                }
            }
        });

        workerThread.start();
    }

    //Checks if the bluetooth is enabled, and tries connecting.
    void checkBluetooth() {
        //Prompt user to turn on Bluetooth if Bluetooth is disabled
        if (!blueAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        try {
            init();
        } catch (IOException e2) {

        }
    }

    void toast(String str) {
        myToast.setText(str);
        myToast.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myToast = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);
        sendPost = (Button)findViewById(R.id.sendPost);
        sendComment = (Button)findViewById(R.id.sendComment);
        codeSurNotice = (TextView)findViewById(R.id.codeSurNotice);
        entrezCode = (TextView)findViewById(R.id.entrezCode);
        texteRecu = (TextView)findViewById(R.id.texteRecu);
        logo = (ImageButton)findViewById(R.id.logo);
        logo.setOnClickListener(logoListener);
        sendPost.setOnClickListener(sendPostListener);
        sendComment.setOnClickListener(sendCommentListener);

        //This code allows connecting to the internet without doing so in a new thread.
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //Handler that receives the data from the bluetooth device.
        //Any manipulation of the received data must be done here (after the messageFinal assignment to msg.obj).
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    toast(messageFinal);
                    messageFinal += (String) msg.obj; //for some reason removing the + causes it to remove the first character
                    texteRecu.setText(messageFinal);
                    if (messageFinal.matches("[\\s\\S]*\\w\\w;[\\s\\S]*")) {
                        char command = messageFinal.charAt(messageFinal.indexOf(";")-2);
                        char arg = messageFinal.charAt(messageFinal.indexOf(";")-1);
                        toast("command = " + command + "\narg = " + arg);
                        if (command == 's') {
                            for (int i = 0; i < subCommands.length; i++) {
                                if (arg == subCommands[i]) {
                                    subreddit = subreddits[i];
                                }
                            }
                            getPost("https://reddit.com/r/"+subreddit+".json");

                        }
                        else if (command == 'p') {
                            postNumber = arg-65;
                            try {
                                getComments("https://reddit.com/" + postIds.get(postNumber) + ".json");
                                toast("Post ID: "+ postIds.get(postNumber));
                            } catch (ArrayIndexOutOfBoundsException e) {
                                toast("Couldn't find a valid post rank, defaulting to the first post.");
                                getComments("https://reddit.com/" + postIds.get(0) + ".json");
                            }
                        }
                        else if (command == 'a' && arg == 'k') {
                            sendString();
                        }
                        else if (command == 'n' && arg == 'p') {
                            /*if (messageAEnvoyer.substring(progressionPage).length() > 5000) {
                                progressionPage += 5000;
                            }*/
                            progressionEnvoi = 0;
                            sendString();
                        }
                    }
                }
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        // connection methods are best here in case program goes into the background etc
        checkBluetooth();
    }



    //Tapping on the logo retries the connection if it failed.
    private OnClickListener logoListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            codeSurNotice.setText("Connexion en cours...");
            try {
                init();
            } catch (IOException e2) {

            }
            codeSurNotice.setText(defaut);
        }
    };

    //Listener for the "send post" button. Is debug and planned to be removed.
    private OnClickListener sendPostListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            messageFinal = "";
            if (inStream != null) {
                beginListenForData();

                getPost("https://reddit.com/r/" + subreddit + ".json");
                sendString();

            } else {
                toast(connectionError);
            }


        }
    };

    //Listener for the "send comment" button, is also debug.
    private OnClickListener sendCommentListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            currentView = false;
            messageFinal = "";
            if (inStream != null) {
                beginListenForData();

                try {
                    getComments("https://reddit.com/" + postIds.get(postNumber) + ".json");
                } catch (ArrayIndexOutOfBoundsException e) {
                    toast("Index out of bounds!");
                }
                toast("Post ID: "+ postIds.get(postNumber));
                sendString();

            } else {
                toast(connectionError);
            }


        }
    };

    void sendString() {
        String aEnvoyer = "";
        messageFinal = "";
        int progTotale = progressionEnvoi + progressionPage;
        //progressionEnvoi = 0;
        if (messageAEnvoyer.substring(progressionPage).length() > 1000 && progressionEnvoi < 4000
                && messageAEnvoyer.substring(progTotale, progTotale+1000).length() > 1000) {
            aEnvoyer = messageAEnvoyer.substring(
                    progTotale, progTotale + 1000) + ",\"";

            progressionEnvoi += 1000;
        } else if (messageAEnvoyer.length() > progressionPage+5000){
            int max = messageAEnvoyer.lastIndexOf(messageAEnvoyer.substring(progTotale,progTotale+1000),'>');
            boolean mettreChevron = false;
            if (max == -1) { //no occurrence of '>'
                max = messageAEnvoyer.lastIndexOf(messageAEnvoyer.substring(progTotale,progTotale+1000),'\n');
                if (max == -1) //no occurrence of '\n'
                    max = progressionEnvoi+progressionPage+1000;
                mettreChevron = true; //there's no occurrence of '>' so we add one
            }

            aEnvoyer = messageAEnvoyer.substring(progressionEnvoi+progressionPage, max);
            if (mettreChevron) {
                aEnvoyer += '>';
                //inserts a '<' after
                messageAEnvoyer = messageAEnvoyer.substring(0, progTotale+max) + '<' + messageAEnvoyer.substring(progTotale+max+1);
            }
            aEnvoyer += ";\"";
            progressionPage += messageAEnvoyer.lastIndexOf('>', progressionEnvoi+progressionPage);
        } else {
            aEnvoyer = messageAEnvoyer.substring(progressionEnvoi+progressionPage) + "\"";
        }
        try {
            write(aEnvoyer);
        } catch (IOException e) {

        } catch (NullPointerException e) {
            toast(connectionError);
        }
    }

    //Parses the json from a reddit url containing comments.
    //Don't try to understand this, it works, that's all you need to know.
    void getComments(String url) {

        String str = "test";
        try {
            str = getText(url);
        } catch (Exception e) {
            toast("Erreur de connexion");
        }
        toast(str);
        String result = "";
        String textPostContent = "";
        String commentText = "";
        String commentAuthor = "";
        String commentScore = "";
        String[] jsonAttributes = {"selftext", "title"};
        String[] cmtVars = {commentAuthor, commentScore, commentText};
        String[] cmtAttributes = {"replies", "author", "score", "body"};

        ArrayList<Integer> commentLvls = new ArrayList<Integer>();
        int currentComment = -1;
        //first, browse through the string
        int commentLvl = 1; //that way it's set to 1 after the }}], "after" of the text post

        System.out.println("Beginning comment level analysis");
        for (int i = 10; i < str.length(); i++) {
            if (str.startsWith("}}], \"after\"", i)) {
                commentLvl--;
            } else if (str.startsWith("[{\"kind\": \"t1\"", i)) {
                commentLvl++;
                commentLvls.add(commentLvl);

            } else if (str.startsWith(" {\"kind\": \"t1\"", i)) {
                commentLvls.add(commentLvl);
            }

        }



        for (int i = 0; i < str.length(); i++) {
            if (str.startsWith("{\"kind\": \"t3\"", i)) {
                int j = 0;
                while (!str.substring(i - 1, i + 11).equals("}}], \"after\"")) {
                    i++;
                    if (j < jsonAttributes.length && str.startsWith("\"" + jsonAttributes[j] + "\": ", i)) {
                        i += jsonAttributes[j].length() + 4;
                        int end = 0;
                        i++;
                        end = i;
                        while (!str.substring(end - 1, end + 3).equals("\", \"")) {
                            end++;
                        }
                        end--;
                        if (j == 1)
                            textPostContent = str.substring(i, end) + "\n" + textPostContent;
                        else
                            textPostContent += str.substring(i, end);
                        j++;
                    }
                }
                result += "<t " + textPostContent + "> \n";
            } else if (str.startsWith("{\"kind\": \"t1\"", i)) {
                currentComment++;
                int l = i;
                int j = 0;
                int k = 0;
                while (!str.substring(l - 1, l + 11).equals("}}], \"after\"")
                        && !str.substring(l - 1, l + 16).equals("}}, {\"kind\": \"t1\"")) {
                    l++;
                    if (j < cmtAttributes.length && str.startsWith("\"" + cmtAttributes[j] + "\": ", l)) {
                        l += cmtAttributes[j].length() + 4;

                        int end = l;
                        if (j == 0) {

                            l += 57;
                            do {
                                if (str.startsWith("{\"kind\": \"t1\"", l)) {
                                    k++;
                                }
                                if (str.startsWith("}}], \"after\"", l)) {
                                    k--;
                                }
                                if (str.startsWith("}}, {\"kind\": \"t1\"", l)) {
                                    l += 10;
                                }
                                l++;
                            } while (k != 0);
                            l++;
                        } else {
                            if (j != 2) {
                                while (!str.substring(end - 1, end + 3).equals("\", \"")) {
                                    end++;
                                }
                                end--;
                                l++;
                            } else {
                                end = str.indexOf(',', l);
                            }
                            cmtVars[j - 1] = str.substring(l, end);
                        }
                        j++;
                    }
                }
                result += "<" + commentLvls.get(currentComment) + " " + cmtVars[2]
                        + "\n/u/" + cmtVars[0] + " " + cmtVars[1] + " upvotes>";
            }
        }
        result = modifyText(result);
        messageAEnvoyer = result;
        progressionEnvoi = 0;
        sendString();
    }

    //Parses the json of a reddit sub (which contains posts)
    void getPost(String url) {
        String str = "test";
        //connects to the given url
        try {
            str = getText(url);
        } catch (Exception e) {
            toast("Erreur de connexion");
        }

        String posts = "";
        String currentPostTitle = "";
        String currentPostUser = "";
        String currentPostId = "";
        String currentPostSub = "";
        String currentPostUrl = "";
        String currentPostScore = "";
        String currentPostCmts = "";
        String[] postAttributes = {currentPostUrl, currentPostSub, currentPostId, currentPostUser, currentPostScore, currentPostCmts, currentPostTitle};
        String[] jsonAttributes = {"domain", "subreddit", "id", "author", "score", "num_comments", "title"};

        for (int i = 0; i < str.length(); i++) { //browses through the string
            if (str.startsWith("{\"kind\": \"t3\"", i)) { //beginning of post
                int j = 0;
                while (!str.substring(i - 1, i + 27).equals("}}, {\"kind\": \"t3\", \"data\": {") //loops while post is not finished
                        && !str.substring(i - 1, i + 11).equals("}}], \"after\"")) {
                    i++;
                    if (j < jsonAttributes.length && str.startsWith("\"" + jsonAttributes[j] + "\": ", i)) { //assigns values of attributes to their variables
                        i += jsonAttributes[j].length() + 4;
                        int end = 0;
                        if (j == 4 || j == 5)
                            end = str.indexOf(',', i);
                        else {
                            i++;
                            end = i;
                            while (!str.substring(end - 1, end + 3).equals("\", \"")) {
                                end++;
                            }
                            end--;
                        }
                        postAttributes[j] = str.substring(i, end);
                        j++;
                    }
                }
                if (postAttributes[0].startsWith("self."))
                    postAttributes[0] = "self";
                else
                    postAttributes[0] = "ext.link";

                postIds.add(postAttributes[2]);
                posts += "<p " + postAttributes[6] + "\n" + postAttributes[0] + " /r/" + postAttributes[1]
                        + " /u/" + postAttributes[3] + " " + postAttributes[4] + " upvotes " + postAttributes[5] + " comments>\n";
            }

        }

        messageAEnvoyer = modifyText(posts);
        progressionEnvoi = 0;
        sendString();
    }

    //Connects to a given url
    public String getText(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        connection.setRequestProperty("User-Agent", "android:com.zezombye.caddit:v1.0 (by /u/Zezombye)");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        return response.toString();
    }

    //Replaces special characters in the json
    static String modifyText(String str) {
        str = str.replaceAll("\\\\\\\"", "&q;"); //replaces \" by &q
        str = str.replaceAll("\\n", "\n"); //replaces \n by the LF character
        str = str.replaceAll("\\\\\\\\", "\\\\"); //replaces \\ by \
        str = str.replaceAll("&amp;", "&");
        return str;
    }

}