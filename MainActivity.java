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
    EditText codeEntered = null;
    TextView entrezCode = null;
    TextView codeSurNotice = null;
    TextView codeAEteChange = null;
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
    String subreddit = "skymine_";

    String connectionError = "PortalStudio n'a pas pu se connecter au portail. Vérifiez votre connexion bluetooth. Tapez sur le logo pour réessayer la connexion.";

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
                        }else{
                            Log.e("error", "Bluetooth is disabled.");
                        }
                    }
                } catch (IOException e) {

                }
            }
        });

        connexionThread.start();

    }

    public void write(String s) throws IOException, NullPointerException {
        outputStream.write(s.getBytes());
    }

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
        // On récupère toutes les vues dont on a besoin
        sendPost = (Button)findViewById(R.id.sendPost);
        sendComment = (Button)findViewById(R.id.sendComment);
        codeSurNotice = (TextView)findViewById(R.id.codeSurNotice);
        entrezCode = (TextView)findViewById(R.id.entrezCode);
        texteRecu = (TextView)findViewById(R.id.texteRecu);
        // On attribue un listener adapté aux vues qui en ont besoin
        logo = (ImageButton)findViewById(R.id.logo);
        logo.setOnClickListener(logoListener);
        sendPost.setOnClickListener(sendPostListener);
        sendComment.setOnClickListener(sendCommentListener);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //codeEntered.addTextChangedListener(textWatcher);
        //codeAEteChange.setVisibility(View.GONE);
        checkBluetooth();

        /*try {
            FileInputStream fIn = openFileInput("codefile.txt");
            InputStreamReader isr = new InputStreamReader(fIn);

            char[] inputBuffer = new char[4];
            isr.read(inputBuffer);

            // Transform the chars to a String
            String readString = new String(inputBuffer);
            code = readString;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            toast("PortalStudio a trouvé le fichier, mais n'a pas pu le lire");
        }*/
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    toast(messageFinal);
                    messageFinal += (String) msg.obj; //for some reason removing the + causes it to remove the first character
                    texteRecu.setText(messageFinal);

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


    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            codeSurNotice.setText(defaut);
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // Uniquement pour le bouton "envoyer"
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

    private OnClickListener sendPostListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            currentView = true;
            messageFinal = "";
            if (inStream != null) {
                beginListenForData();

                String aEnvoyer;

                if (currentView)
                    aEnvoyer = getPost("https://reddit.com/r/" + subreddit + ".json") + "\"";
                else
                    aEnvoyer = "<p test post> <1 test comment> <2 test second comment>\"\n";

                try {
                    write(aEnvoyer);
                } catch (IOException e) {

                } catch (NullPointerException e) {
                    toast(connectionError);
                }
            } else {
                toast(connectionError);
            }


        }
    };

    private OnClickListener sendCommentListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            currentView = false;
            messageFinal = "";
            if (inStream != null) {
                beginListenForData();

                String aEnvoyer;

                if (currentView)
                    aEnvoyer = "<r /r/the_donald> <p t=YOU CAN'T STUMP THE TRUMP\" a=/r/the_donald 4324 upvotes>Z";
                else
                    aEnvoyer = "<r " + subreddit + "> " + getComments("https://reddit.com/" + postIds.get(postNumber) + ".json");
                toast(postIds.get(postNumber));

                try {
                    write(aEnvoyer);
                } catch (IOException e) {

                } catch (NullPointerException e) {
                    toast(connectionError);
                }
            } else {
                toast(connectionError);
            }


        }
    };

    String getComments(String url) {

        String str = "test";
        try {
            str = getText(url);
        } catch (Exception e) {}
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
            //System.out.println(str.substring(i-1, i+11));
            if (str.startsWith("}}], \"after\"", i)) {
                commentLvl--;
					/*String tabs = "";
					for (int j = 0; j < commentLvl; j++)
						tabs += "\t";
					System.out.println(tabs + "End of comment found. lvl = " + commentLvl);*/
            } else if (str.startsWith("[{\"kind\": \"t1\"", i)) {
                commentLvl++;
                commentLvls.add(commentLvl);
                //commentLvl++;

            } else if (str.startsWith(" {\"kind\": \"t1\"", i)) {
                commentLvls.add(commentLvl);
            }

        }


			/*boolean containsReplies = false;
			boolean isReply = false;
			boolean nextCmtIsALvlDown = false;*/

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
                            //System.out.println(str.substring(end-1, end+3));
                            end++;
                        }
                        end--;
                        if (j == 1)
                            textPostContent = str.substring(i, end) + "\n" + textPostContent;
                        else
                            textPostContent += str.substring(i, end);
                        //System.out.println("test2 " + postAttributes[j]);
                        //System.out.println("test" + jsonAttributes[j]);
                        j++;
                    }
                }
                result += "<t " + textPostContent + "> \n";
            } else if (str.startsWith("{\"kind\": \"t1\"", i)) {
                currentComment++;
					/*if (containsReplies)
						isReply = true;
					else if (str.charAt(i-1) == '[')
						isReply = false;*/
                //System.out.println("*****************************\nComment found.");
                //System.out.println(str.charAt(i-1));
                //System.out.println("is reply = " + isReply);
                //System.out.println("previous comment contains replies : " + containsReplies);
                int l = i;
                int j = 0;
                int k = 0;
                while (!str.substring(l - 1, l + 11).equals("}}], \"after\"")
                        && !str.substring(l - 1, l + 16).equals("}}, {\"kind\": \"t1\"")) {
                    l++;
                    if (j < cmtAttributes.length && str.startsWith("\"" + cmtAttributes[j] + "\": ", l)) {
                        l += cmtAttributes[j].length() + 4;

                        //l++;
                        int end = l;
                        //System.out.println("Attribute found: " + cmtAttributes[j]);
                        if (j == 0) {

                            //System.out.println("Skipping possible child comments.");
                            l += 57;
                            //System.out.println(str.substring(l));
                            //containsReplies = false;
                            do {
                                if (str.startsWith("{\"kind\": \"t1\"", l)) {
                                    k++;
                                    //containsReplies = true; //if there are replies then the comment level goes up
                                    //System.out.println("Child comment found, lvl " + (commentLvl+k));
                                }
                                if (str.startsWith("}}], \"after\"", l)) {
                                    k--;
                                    //System.out.println("Child comment finished, k = " + k);
                                }
                                if (str.startsWith("}}, {\"kind\": \"t1\"", l)) {
                                    //System.out.println("Child comment finished, another one found. k = " + k);
                                    l += 10;
                                }
                                l++;
                            } while (k != 0);
                            l++;
                        } else {
                            if (j != 2) {
                                while (!str.substring(end - 1, end + 3).equals("\", \"")) {
                                    //System.out.println(str.substring(end-1, end+3));
                                    end++;
                                }
                                end--;
                                l++;
                            } else {
                                end = str.indexOf(',', l);
                                //end--;
                            }
                            //end--;
                            cmtVars[j - 1] = str.substring(l, end);
                        }
                        j++;
                    }
                }
					/*if (isReply)// && !str.substring(l-1, l+11).equals("}}], \"after\""))
						if (nextCmtIsALvlDown)
							commentLvl--;
						else
							commentLvl++;
					else // if (!isReply)// if (str.charAt(i-1) == '[')
						commentLvl = 0;

					if (str.substring(l-1, l+11).equals("}}], \"after\""))
						nextCmtIsALvlDown = true;*/
                result += "<" + commentLvls.get(currentComment) + " " + cmtVars[2]
                        + "\n/u/" + cmtVars[0] + " " + cmtVars[1] + " upvotes>";
            }
        }
        result = modifyText(result);
        return result + "\"";
    }

    String getPost(String url) {
        String str = "test";
        do {
            /*new Thread( new Runnable() {

            }).start();*/
            try {
                str = getText(url);
            } catch (Exception e) {}
        } while (str.equals("test"));
        toast(str);
        //toast ("Finished getting text");

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

        for (int i = 0; i < str.length(); i++) {
            if (str.startsWith("{\"kind\": \"t3\"", i)) {
                int j = 0;
                while (!str.substring(i - 1, i + 27).equals("}}, {\"kind\": \"t3\", \"data\": {")
                        && !str.substring(i - 1, i + 11).equals("}}], \"after\"")) {
                    i++;
                    if (j < jsonAttributes.length && str.startsWith("\"" + jsonAttributes[j] + "\": ", i)) {
                        i += jsonAttributes[j].length() + 4;
                        int end = 0;
                        if (j == 4 || j == 5)
                            end = str.indexOf(',', i);
                        else {
                            i++;
                            end = i;
                            while (!str.substring(end - 1, end + 3).equals("\", \"")) {
                                //System.out.println(str.substring(end-1, end+3));
                                end++;
                            }
                            end--;
                        }
                        postAttributes[j] = str.substring(i, end);
                        //System.out.println("test2 " + postAttributes[j]);
                        //System.out.println("test" + jsonAttributes[j]);
                        j++;
                    }
                }
                //System.out.println("fin de post");
                if (postAttributes[0].startsWith("self."))
                    postAttributes[0] = "self";
                else
                    postAttributes[0] = "ext.link";
                postIds.add(postAttributes[2]);
                posts += "<p " + postAttributes[6] + "\n" + postAttributes[0] + " /r/" + postAttributes[1]
                        + " /u/" + postAttributes[3] + " " + postAttributes[4] + " upvotes " + postAttributes[5] + " comments>\n";
            }

        }
        //toast("Finished result");
        return posts + "\"";
    }

    public String getText(String url) throws Exception {
        //toast("Trying to connect to " + url);
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
        //toast("Successfully connected");
        return response.toString();
    }

    static String modifyText(String str) {
        str = str.replaceAll("\\\\\\\"", "&q;"); //transforme \" en "
        str = str.replaceAll("\\n", "\n"); //transforme \n en nouvelle ligne
        str = str.replaceAll("\\\\\\\\", "\\\\"); // transforme \\ en \
        str = str.replaceAll("&amp;", "&");
        return str;
    }

}