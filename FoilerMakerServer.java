import com.sun.corba.se.impl.io.OutputStreamHook;
import com.sun.corba.se.spi.activation.Server;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Yufei Xu on 11/20/2016.
 */
public class FoilerMakerServer {
    String instance[] = new String[3];
    static HashMap<String, String> user = new HashMap<String, String>();//username, password||after loggin, usertoken, username
    static HashMap<String, Boolean> user1 = new HashMap<String, Boolean>();//username,loggin(in:false,not loggin:true)
    static HashMap<String, Boolean> user2 = new HashMap<String, Boolean>();//usertoken,ingame(in:false,not in:true)
    static HashMap<String, String> user3 = new HashMap<String, String>();//usertoken,key
    static HashMap<String, String> user4 = new HashMap<String, String>();//usertoken,score:fool:fooled
    static HashMap<String, String> Question = new HashMap<String, String>();//question, answer
    static HashMap<String, String> Suggestion = new HashMap<String, String>();//quetiong,suggestion
    static String[] ques = new String[10];
    static String wenti = new String();
    File f = new File("UserDatabase");
    File W = new File("WordleDeck");
    static ServerSocket ssocket = null;
    //static Socket socket = null;
    InputStream input = null;
    //static OutputStream out = null;
    //static PrintWriter p = null;
    static String usertoken =new String();
    static int quesnum=-1;
    static int serverPort;
    static boolean next=false;
    static String rec = new String();
    static String send =new String();

    public static void main(String[] args) {
        FoilerMakerServer f = new FoilerMakerServer();
        Scanner scanner  = new Scanner(System.in);
        System.out.println("Please Enter the port number");
        serverPort = scanner.nextInt();
        try{
            f.loadInformation();
            f.loadQuestion();
            ssocket = new ServerSocket(serverPort);
            f.run();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() throws IOException {
        final Socket socket1 = ssocket.accept();
                Socket socket = socket1;
                System.out.println("Connected");
                OutputStream out = socket.getOutputStream();
                PrintWriter p = new PrintWriter(out,true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                next=false;
                while(next==false){
                    rec = recieve(in);
                    System.out.println(rec);
                    Register(rec);

                    System.out.println(send);
                    p.println(send);
                }


        //run();

    }

    public void Register(String s){
        String com = getMessage(s,0);
        String username = getMessage(s,1);
        String password = getMessage(s,2);
        String alph = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String under = "_";
        String num = "1234567890";
        String uppercase="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String other = "#&$*";
        boolean passvalid = true;
        for(int i = 0;i<password.length();i++){
            if(alph.indexOf(password.charAt(i))==-1){
                if(other.indexOf(password.charAt(i))==-1)
                    passvalid = false;
            }
        }
        boolean passhasUpper = false;
        for(int i =0;i<password.length();i++){
            if(uppercase.indexOf(password.charAt(i))!=-1)
                passhasUpper = true;
        }
        boolean passhasnum = false;
        for(int i =0;i<password.length();i++){
            if(num.indexOf(password.charAt(i))!=-1)
                passhasnum = true;
        }
        boolean uservalid=true;
        for(int i =0;i<username.length();i++){
            if(alph.indexOf(username.charAt(i))==-1){
                if(under.indexOf(username.charAt(i))==-1)
                uservalid=false;
            }

        }
        if(!com.equals("CREATENEWUSER")||!com.equals("LOGIN")){
            send = "RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT";
        }
        else if(com.equals("CREATENEWUSER")) {
         if (username.length() == 0 || username.length() > 10 || uservalid) {
                send = "RESPONSE--CREATENEWUSER--INVALIDUSERNAME";
            } else if (password.length() == 0 || password.length() > 10 || passhasnum || passhasUpper || passvalid)
                send = "RESPONSE--CREATENEWUSER--INVALIDUSERPASSWORD";
            else if (user.containsKey(username))
                send = "RESPONSE--CREATENEWUSER--USERALREADYEXISTS";
            else {
                send = "RESPONSE--CREATENEWUSER--SUCCESS";
            }
        }
        else if(com.equals("LOGIN")){
            if(user.containsKey(username))
                send = "RESPONSE--CREATENEWUSER--UNKNOWNUSER";
            else if(user.containsValue(password))
                send = "RESPONSE--CREATENEWUSER--INVALIDUSERPASSWORD";
            else if(user1.get(username))
                send = "RESPONSE--CREATENEWUSER--USERALREADYLOGGEDIN";
            else{
                next=true;
                usertoken = usertoken();
                user.remove(username);
                user.put(usertoken,username);
                user1.remove(username);
                user1.put(username,false);
                user2.put(usertoken,true);
                send = "RESPONSE--CREATENEWUSER--SUCCESS--"+usertoken;
            }

        }
        System.out.println(send);




    }
/*
    public void login(String s){
        String com = getMessage(s,1);
        String username = getMessage(s,2);
        String password = getMessage(s,3);
        if(!com.equals("LOGIN")){
            send = "RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT";
        }
        else if(user.containsKey(username))
            send = "RESPONSE--CREATENEWUSER--UNKNOWNUSER";
        else if(user.containsValue(password))
            send = "RESPONSE--CREATENEWUSER--INVALIDUSERPASSWORD";
        else if(user1.get(username))
            send = "RESPONSE--CREATENEWUSER--USERALREADYLOGGEDIN";
        else{
            next=true;
            usertoken = usertoken();
            user.remove(username);
            user.put(usertoken,username);
            user1.remove(username);
            user1.put(username,false);
            user2.put(usertoken,true);
            send = "RESPONSE--CREATENEWUSER--SUCCESS--"+usertoken;
        }

    }
    */
    /*

    public void StartNewGame(String s){
        String com = getMessage(s,1);
        String token = getMessage(s,2);
        String send;
        if(user2.containsKey(token))
            send = "RESPONSE--STARTNEWGAME--USERNOTLOGGEDIN";
        else if(user2.get(token))
            send = "RESPONSE--STARTNEWGAME--FAILURE";
        else{
            user2.remove(token);
            user2.put(token,false);
            String key = key();
            user3.put(token,key);
            send = "RESPONSE--STARTNEWGAME--SUCCESS--"+key;
        }
        try {
            send(send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //how to send to another client
    public void JoinGame(String s){
        String com = getMessage(s,1);
        String token = getMessage(s,2);
        String key = getMessage(s,3);
        String send;
        if(user2.containsKey(token)) {
            send = "RESPONSE--JOINGAME--USERNOTLOGGEDIN--" + token;
            try {
                send(send);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(user3.containsValue(key)) {
            send = "RESPONSE--JOINGAME--GAMEKEYNOTFOUND--" + token;
            try {
                send(send);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(user2.get(token)) {
            send = "RESPONSE--JOINGAME--FAILURE--" + token;
            try {
                send(send);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            send = "RESPONSE--JOINGAME--SUCCESS--"+token;
            String name=user.get(token);
            String infor = user4.get(name);
            int score=getScore(infor);
            infor="NEWPARTICIPANT--"+name+"--"+score;
            try {
                send(send);
                send(infor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void LaunchGame(String s){
        quesnum++;
        String com=getMessage(s,1);
        String token = getMessage(s,2);
        String key = getMessage(s,3);
        String send;
        if(user.containsKey(token))
            send = "RESPONSE---ALLPARTICIPANTSHAVEJOINED--USERNOTLOGGEDIN";
        else if(user3.containsValue(key))
            send = "RESPONSE---ALLPARTICIPANTSHAVEJOINED--INVALIDGAMETOKEN";
        else if(user2.get(token))
            send = "RESPONSE---ALLPARTICIPANTSHAVEJOINED--USERNOTGAMELEADER";
        else{
            try {
                loadInformation();
                wenti = ques[quesnum];
                send = "NEWGAMEWORD--"+ques[quesnum]+"--"+Question.get(ques[quesnum]);
                send(send);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void CollectSuggestions(String s)throws IOException{
        String com = getMessage(s,1);
        String token = getMessage(s,2);
        String key = getMessage(s,3);
        String suggestion = getMessage(s,4);
        String send;
        if(user.containsKey(token)) {
            send = "RESPONSE--PLAYERSUGGESTION--USERNOTLOGGEDIN";
            send(send);
        }
        else if(user3.containsValue(key)){
            send = "RESPONSE--PLAYERSUGGESTION--INVALIDGAMETOKEN";
            send(send);
        }
        else if(!com.equals("PLAYERSUGGESTION")){
            send = "RESPONSE--PLAYERSUGGESTION--INVALIDMESSAGEFORMAT";
            send(send);
        }
        else{
            Suggestion.put(wenti,suggestion);
        }

    }

    public void SendOptions(){
        Collection<String> s =Suggestion.values();
        Object[] a = s.toArray();
        String suggestion[] = new String[a.length];
        for(int i=0;i<a.length;i++){
            suggestion[i]=a[i].toString();
        }
        String send = "ROUNDOPTIONS";
        for(int i=0;i<suggestion.length;i++){
            send = send + "--"+suggestion[i];
        }

        try{
            send(send);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
*/




    private void loadInformation()throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = new String();
        int i = 0;
        while((line = br.readLine())!=null){
            int a =line.indexOf(":");
            int b = line.length();
            String name = line.substring(0,a);
            String left = line.substring(a+1,b);
            a = left.indexOf(":");
            b = left.length();
            String password = left.substring(0,a);
            left = left.substring(a+1,b);
            String value[][] = new String[1][2];
            value[0][0]= password;
            value[0][1]= left;
            user.put(name,password);
            user1.put(name,true);
            user4.put(name,left);
        }

    }
    private void write(String user1,String pass) throws FileNotFoundException {
        FileOutputStream out = new FileOutputStream(f);
        PrintWriter p = new PrintWriter(out);
        user.put(user1,pass);
        p.println(user1+":"+pass+":0:0:0");



    }
    private void loadQuestion()throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(W));
        String line = new String();
        int i = 0;
        while((line = br.readLine())!=null){
            int a =line.indexOf(":");
            int b = line.length();
            String question = line.substring(0,a);
            ques[i]=question;
            String answer = line.substring(a+1,b);
            Question.put(question,answer);
            i++;
        }



    }



    public String getMessage(String message, int i){
        for(int j=0;j<i;j++) {
            int l = message.length();
            int a = message.indexOf("-");
            if(a==-1)
                return null;
            message = message.substring(a + 2, l);
        }
        int a = message.indexOf("-");
        if(a==-1)
            return message;
        else
            message = message.substring(0,a);
        return message;


    }


    private String usertoken(){
        String alph = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String token = new String();
        for(int i=0;i<10;i++){
            int j = (int)(0+Math.random()*51);
            char a = alph.charAt(j);
            token += a;
        }
        return token;
    }
    private String key(){
        String alph = "abcdefghijklmnopqrstuvwxyz";
        String key = new String();
        for(int i=0;i<3;i++){
            int j = (int)(0+Math.random()*25);
            char a = alph.charAt(j);
            key += a;
        }
        return key;
    }

    private int getScore(String s){
       int b = s.indexOf(":");
        int a = Integer.parseInt(s.substring(0,b));
        return a;
    }

    private int getFooltimes(String s){
        int a = s.indexOf(":");
        String b = s.substring(0,a);
        a = b.indexOf(":");
        int c = Integer.parseInt(b.substring(0,a));
        return c;
    }

    private int getFooledtimes(String s){
        int a = s.indexOf(":");
        String b = s.substring(0,a);
        a = b.indexOf(":");
        b = b.substring(0,a);
        a = b.indexOf(":");
        int c = Integer.parseInt(b.substring(0,a));
        return c;
    }

    public String recieve(BufferedReader f) throws IOException{
        String a = null;
        while(a==null){
            try{
                a=f.readLine();
                System.out.println(a);
            }catch (SocketTimeoutException e){

            }
        }
        return a;
    }
}
