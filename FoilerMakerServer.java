import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Yufei Xu on 11/20/2016.
 */
public class FoilerMakerServer{
    static HashMap<String, String> user = new HashMap<String, String>();//username, password||after loggin, usertoken, username
    static HashMap<String, Boolean> user1 = new HashMap<String, Boolean>();//username,loggin(in:true,not loggin:false)
    static HashMap<String, Boolean> user2 = new HashMap<String, Boolean>();//usertoken,ingame(in:true,not in:false)
    static HashMap<String, String> user3 = new HashMap<String, String>();//usertoken,key
    static HashMap<String, String> user4 = new HashMap<String, String>();//usertoken,score:fool:fooled
    static HashMap<String, String> Question = new HashMap<String, String>();//question, answer
    static HashMap<String, String> Suggestion = new HashMap<String, String>();//token,suggestion
    static HashMap<String, String> Suggestion1 = new HashMap<String, String>();//suggestion,token
    static HashMap<String, String> Choice  = new HashMap<String, String>();//token, choice
    static HashMap<String, String> Choice1 = new HashMap<String, String>();//choice, token
    static HashMap<String, String> Reply = new HashMap<String, String>();
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
    static int finish=0;
    static boolean next=false;
    static String send =new String();
    static String infor = null;
    static boolean startgame= false;
    //static boolean allsuggestion=false;

    public static void main(String[] args) {
        FoilerMakerServer f = new FoilerMakerServer();
        Scanner scanner  = new Scanner(System.in);
        System.out.println("Please Enter the port number");
        serverPort = scanner.nextInt();
        try{
            f.loadQuestion();
            f.loadInformation();
            ssocket = new ServerSocket(serverPort);
            f.run();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        Socket socket1 = null;
        try {
            socket1 = ssocket.accept();
        }catch (IOException e1){
        }

        final Socket finalSocket = socket1;
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    String rec = new String();
                    Socket socket = finalSocket;
                    System.out.println("Connected");
                    OutputStream out = socket.getOutputStream();
                    final PrintWriter p = new PrintWriter(out, true);
                    final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    next = false;
                    while (next == false) {
                        rec = recieve(in);
                        Register(rec);
                        p.println(send);
                    }

                    next = false;
                    while(next==false){
                        rec = recieve(in);
                        StartNewGame(rec);
                        p.println(send);
                    }

                    if(getMessage(rec,0).equals("STARTNEWGAME")) {
                        SwingWorker worker = new SwingWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {
                                while(startgame == false) {
                                    while (infor == null) {
                                        System.out.print("");

                                    }
                                    System.out.println(infor);
                                    p.println(infor);
                                    infor=null;


                                }
                                return null;
                            }
                        };
                        worker.execute();
                        next = false;
                        while(next==false) {
                            rec = recieve(in);
                            LaunchGame(rec);

                        }
                    }
                    if(getMessage(rec,0).equals("JOINGAME")){
                        while(!getMessage(send,0).equals("NEWGAMEWORD")){
                            System.out.print("");
                        }
                    }
                    int quesnumber=0;
                    while(quesnum<Question.size()){
                        finish=0;
                        quesnum=quesnumber;
                    sendQuestion();
                    p.println(send);

                    next=false;
                    while(next==false){
                        rec =recieve(in);
                        CollectSuggestions(rec);


                    }

                    SendOptions();
                    p.println(send);

                    next=false;
                    while(next==false){
                        rec = recieve(in);
                        CollectChoice(rec);
                        if(next==false) {
                            p.println(send);
                        }
                    }
                    next=false;
                    send="";
                    while(next==false) {
                        makeReply(rec);
                    }
                    send = "ROUNDRESULT";

                    Collection<String> c  = Reply.values();
                    Object[] a = c.toArray();
                    String d[] = new String[a.length];
                    for(int i=0;i<a.length;i++){
                        d[i]=a[i].toString();
                    }

                    for(int i=0;i<d.length;i++){
                        send = send+d[i];
                    }
                    finish++;
                    while(finish!=user2.size()){
                        System.out.print("");
                    }
                    p.println(send);
Suggestion.clear();
                        Suggestion1.clear();
                        quesnumber++;
                        quesnum=quesnumber;
                    }
                    send ="GAMEOVER";
                    p.println(send);


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        worker.execute();
        run();



        //run();

        }

    public void Register(String s) throws IOException {
        String com = getMessage(s,0);
        String username = getMessage(s,1);
        String password = getMessage(s,2);
        String alph = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String under = "_";
        String num = "1234567890";
        String uppercase="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String other = "#&$*";

        if(!com.equals("CREATENEWUSER")&&!com.equals("LOGIN")){
            send = "RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT";
        }
        else if(com.equals("CREATENEWUSER")) {
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
         if (username.length() == 0 || username.length() > 10 || uservalid==false) {
                send = "RESPONSE--CREATENEWUSER--INVALIDUSERNAME";
            } else if (password.length() == 0 || password.length() > 10 || passhasnum==false || passhasUpper==false || passvalid==false) {
             send = "RESPONSE--CREATENEWUSER--INVALIDUSERPASSWORD";
             System.out.println(passhasnum+""+passhasUpper+""+passvalid);
         }
            else if (user.containsKey(username))
                send = "RESPONSE--CREATENEWUSER--USERALREADYEXISTS";
            else {
                send = "RESPONSE--CREATENEWUSER--SUCCESS";

             write(username,password);
             loadInformation();
            }
        }
        else if(com.equals("LOGIN")){

            if(user.containsKey(" "+username)) {
                System.out.println(username);
                System.out.println(user);
                send = "RESPONSE--LOGIN--UNKNOWNUSER";
            }
            else if(user.containsValue(password)==false) {
                System.out.println(user);
                send = "RESPONSE--LOGIN--INVALIDUSERPASSWORD";
            }
            else if(user1.get(username)) {
                send = "RESPONSE--LOGIN--USERALREADYLOGGEDIN";

            }
            else{
                next=true;
                usertoken = usertoken();
                user.remove(username);
                user.put(usertoken,username);
                user1.remove(username);

                user1.put(username,true);
                user2.put(usertoken,false);
                send = "RESPONSE--LOGIN--SUCCESS--"+usertoken;
            }
            System.out.println(send);

        }





    }
    public void StartNewGame(String s) {
        String com = getMessage(s, 0);
        String token = getMessage(s, 1);
        if (com.equals("STARTNEWGAME")) {
            if (user2.containsKey(" "+token))
                send = "RESPONSE--STARTNEWGAME--USERNOTLOGGEDIN";
            else if (user2.get(token))
                send = "RESPONSE--STARTNEWGAME--FAILURE";
            else {
                next=true;
                user2.remove(token);
                user2.put(token, true);
                String key = key();
                user3.put(token, key);
                send = "RESPONSE--STARTNEWGAME--SUCCESS--" + key;
            }

        }

        if(com.equals("JOINGAME")) {
            token = getMessage(s,1);
            String key = getMessage(s, 2);
            if (user2.containsKey(" "+token)) {
                send = "RESPONSE--JOINGAME--USERNOTLOGGEDIN--" + token;
            }
            if (user3.containsValue(key)) {
                send = "RESPONSE--JOINGAME--GAMEKEYNOTFOUND--" + token;
            }
            if (user2.get(token)) {
                send = "RESPONSE--JOINGAME--FAILURE--" + token;
            } else {
                next=true;
                send = "RESPONSE--JOINGAME--SUCCESS--" + key;
                String name = user.get(token);
                user2.remove(token);
                user2.put(token,true);
                infor = user4.get(name);
                int score = getScore(infor);
                infor = "NEWPARTICIPANT--" + name + "--" + score;
            }
        }

    }
    public void LaunchGame(String s){

        String com=getMessage(s,0);
        String token = getMessage(s,1);
        String key = getMessage(s,2);
        if(user.containsKey(" "+token)) {
            send = "RESPONSE---ALLPARTICIPANTSHAVEJOINED--USERNOTLOGGEDIN";
        }
        else if(user3.containsValue(" "+key)) {
            send = "RESPONSE---ALLPARTICIPANTSHAVEJOINED--INVALIDGAMETOKEN";
        }
        else if(user3.containsKey(" "+token)) {
            send = "RESPONSE---ALLPARTICIPANTSHAVEJOINED--USERNOTGAMELEADER";
        }
        else{
            try {

                next =true;
                startgame=true;
                loadInformation();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void sendQuestion(){
        wenti = ques[quesnum];
        send = "NEWGAMEWORD--"+ques[quesnum]+"--"+Question.get(ques[quesnum]);
    }


    public void CollectSuggestions(String s)throws IOException{
        String com = getMessage(s,0);
        String token = getMessage(s,1);
        String key = getMessage(s,2);
        String suggestion = getMessage(s,3);


        if(user.containsKey(" "+token)) {

            send = "RESPONSE--PLAYERSUGGESTION--USERNOTLOGGEDIN";
        }
        else if(user3.containsValue(" "+key)){

            send = "RESPONSE--PLAYERSUGGESTION--INVALIDGAMETOKEN";
        }
        else if(!com.equals("PLAYERSUGGESTION")){

            send = "RESPONSE--PLAYERSUGGESTION--INVALIDMESSAGEFORMAT";
        }
        else{

            Suggestion.put(token,suggestion);
            Suggestion1.put(suggestion,token);
            while(user2.size()!=Suggestion.size()){
                System.out.print("");
            }
            next=true;

        }

    }
    public void SendOptions(){
        Collection<String> s =Suggestion.values();
        Object[] a = s.toArray();
        String suggestion[] = new String[a.length];
        for(int i=0;i<a.length;i++){
            suggestion[i]=a[i].toString();
        }
        send = "ROUNDOPTIONS";
        for(int i=0;i<suggestion.length;i++){
            send = send + "--"+suggestion[i];
        }
        send = send+"--"+Question.get(ques[quesnum]);

    }
    public void CollectChoice(String s){
        String com = getMessage(s,0);
        String token = getMessage(s,1);
        String key = getMessage(s,2);
        String choice = getMessage(s,3);
        String name = user.get(token);

        if(user.containsKey(" "+token)) {
            send = "RESPONSE--PLAYERCHOICE--USERNOTLOGGEDIN";
        }
        else if(user3.containsValue(" "+key)){
            send = "RESPONSE--PLAYERCHOICE--INVALIDGAMETOKEN";
        }
        else if(!com.equals("PLAYERCHOICE")){
            send = "RESPONSE--PLAYERCHOICE--INVALIDMESSAGEFORMAT";
        }
        else{

Choice.put(token,choice);
            Choice1.put(choice,token);
            next=true;
        }

    }
    public void makeReply(String s) {
        String token = getMessage(s, 1);
        String name = user.get(token);
        String infor1 = user4.get(name);
        String choice = getMessage(s,3);
        int score = getScore(infor1);
        int fool = getFooltimes(infor1);
        int fooled = getFooledtimes(infor1);
        if (Choice.get(token).equals(Question.get(ques[quesnum]))) {
            if (Choice1.containsKey(Suggestion.get(token))) {
                fool++;
                score += 5;
                score += 10;
                send = send + "--" + name + "--"+ "You got it right! You fooled " + user.get(Choice1.get(Suggestion.get(token))) + "--" + score + "--" + fool + "--" + fooled;

            } else {
                score += 10;
                send = send + "--" + name + "--"+ "You got it right!" + "--" + score + "--" + fool + "--" + fooled;
            }
        } else if (!Choice.get(token).equals(Suggestion.get(token))) {
            send = send + "--" + name + "--"+ "" + "--" + score + "--" + fool + "--" + fooled;
        } else {
            if (Choice1.containsKey(Suggestion.get(token))) {
                fooled++;
                fool++;
                score += 5;
                send = send + "--" + name + "--"+ "You were fooled by "+user.get(Suggestion1.get(choice)) + "You fooled " + user.get(Choice1.get(Suggestion.get(token))) + "--" + score + "--" + fool + "--" + fooled;
            }
            else{

                fooled++;
                send = send +"--"+name+ "--"+" You were fooled by "+user.get(Suggestion1.get(choice))+"--"+score+"--"+fool+"--"+fooled;
            }


        }
        System.out.println(send);
        Reply.put(token,send);
        next=true;
    }





    private void loadInformation()throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = new String();
        int i = 0;
        while((line = br.readLine())!=null) {
            int a = line.indexOf(":");
            int b = line.length();
            String name = line.substring(0, a);
            String left = line.substring(a + 1, b);
            a = left.indexOf(":");
            b = left.length();
            String password = left.substring(0, a);
            left = left.substring(a + 1, b);
            String value[][] = new String[1][2];
            value[0][0] = password;
            value[0][1] = left;
            user.put(name,password);
            user1.put(name,false);
            user4.put(name,left);
        }


    }
    private void write(String user1,String pass) throws IOException {
        FileWriter fw = new FileWriter(f,true);
        BufferedWriter out = new BufferedWriter(fw);
        String a=user1 + ":" + pass + ":0:0:0";
        out.newLine();
        out.write(a);
        out.close();
        fw.close();
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
        int d = s.length();
        String b = s.substring(a+1,d);
        a = b.indexOf(":");
        int c = Integer.parseInt(b.substring(0,a));
        return c;
    }

    private int getFooledtimes(String s){
        int a = s.indexOf(":");
        int d = s.length();
        String b = s.substring(a+1,d);
        a = b.indexOf(":");
        b = b.substring(a+1,b.length());
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
