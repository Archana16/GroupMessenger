package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String[] REMOTE_PORT = {"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT = 10000;
    static String myPort;
    static int count =0;
    static final String URL = "edu.buffalo.cse.cse486586.groupmessenger1.provider";

    public static Uri providerUri =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);


        //calculating the port that this avd listens on
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        providerUri = buildUri("content", URL);
        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */

        final EditText editText = (EditText) findViewById(R.id.editText1);

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        final Button button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    String msg = editText.getText().toString() + "\n";
                    editText.setText("");
                    TextView localText= (TextView) findViewById(R.id.editText1);
                    //localText.append("\t" + msg);

                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

                }


        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }


    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                for(String remotePort : REMOTE_PORT){

                    //code to send out a message
                    //open socket
                    //open stream
                    //write to the stream

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                        String msgToSend = msgs[0].trim();
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        out.println(msgToSend);

                    out.close();
                    socket.close();


                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException "+e);
            }

            return null;
        }
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {

                //code to receive a message
                //open socket
                //open stream
                //read from the stream

            ServerSocket serverSocket = sockets[0];
            try {

                while(true){
                Socket listener = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(listener.getInputStream()));
                    String line= null;

                    while((line= in.readLine()) != null)
                    {
                        System.out.print("check inside"+line);
                        publishProgress(line);
                        line=null;
                    }
                    System.out.println(line);
                    line=null;

                }

            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            System.out.print("check"+strReceived);
            TextView chatTextView = (TextView) findViewById(R.id.textView1);
            chatTextView.append(strReceived + "\t\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

           //invoke content provider functions from content resolver

            ContentValues keyValueToInsert = new ContentValues();
            keyValueToInsert.put("key",Integer.toString(count));
            keyValueToInsert.put("value",strReceived);
            count++;

            Log.e(TAG,providerUri.toString() );
            getContentResolver().insert(
                    providerUri, keyValueToInsert);

            return;
        }
    }



}
