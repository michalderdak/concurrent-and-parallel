import java.*;
import java.io.*;
import java.util.*;
import akka.actor.*;


//// CRYPTO
class KeyPair implements Serializable {
    public final int public_key, private_key;

    public KeyPair(int public_key, int private_key) {
        this.public_key = public_key;
        this.private_key = private_key;
    }
}

class Crypto {
    static KeyPair keygen() {
        int public_key = (new Random()).nextInt(25) + 1;
        int private_key = 26 - public_key;
        System.out.println("public key: " + public_key);
        System.out.println("private key: " + private_key);
        return new KeyPair(public_key, private_key);
    }

    static String encrypt(String cleartext, int key) {
    StringBuffer encrypted = new StringBuffer();
    for (int i=0; i<cleartext.length(); i++) {
        encrypted.append((char) ('A' + ((((int)
        cleartext.charAt(i)) - 'A' + key) % 26)));        
    }
    return "" + encrypted;
    }
}

 //// ACTORS
class RegistryActor extends UntypedActor {
    private Map<ActorRef,KeyPair> secrets; 

    public void onReceive(Object o) throws Exception {
        if (o instanceof InitMessage) {
            secrets = new HashMap<ActorRef,KeyPair>();
            System.out.println("Registry initiated");

        }else if(o instanceof RegisterMessage){
            System.out.println("Receiver registered");
            var secret = Crypto.keygen();
            secrets.put(getSender(),secret);
            getSender().tell(new RegisterMessageResponse(secret),getSelf());

        }else if( o instanceof LookUpMessage){
            LookUpMessage message = (LookUpMessage) o;
            var result = secrets.get(message.Actor);
            getSender().tell(new PublicKeyResponseMessage(result.public_key, message.Actor),getSelf());
        }
        
    }
}

class SenderActor extends UntypedActor {
    public ActorRef RegistryRef;

    public void onReceive(Object o) throws Exception {
        if (o instanceof InitMessage) {
            InitMessage message = (InitMessage) o;
            RegistryRef = message.r;
            System.out.println("Sender initiated");

        }else if (o instanceof CommunicationMessage){
            CommunicationMessage message = (CommunicationMessage) o;
            RegistryRef.tell(new LookUpMessage(message.ActorToLookup), getSelf());

        }else if (o instanceof PublicKeyResponseMessage){
            PublicKeyResponseMessage message = (PublicKeyResponseMessage) o;
            var secretMessage = "SECRET";
            System.out.println("Plain text:" + secretMessage);
            var res = Crypto.encrypt(secretMessage,message.Public_key);
            System.out.println("Encrypted " + res);
            message.Actor.tell(new EncryptedMessage(res),ActorRef.noSender());
        }
    }    
}

class RecieverActor extends UntypedActor {
    public ActorRef RegistryRef;
    private KeyPair Secret;

    public void onReceive(Object o) throws Exception {
        if (o instanceof InitMessage) {
            InitMessage message = (InitMessage) o;
            RegistryRef = message.r;
            System.out.println("Receiver initiated");
            
            RegistryRef.tell(new RegisterMessage(),getSelf());
                
        }else if(o instanceof RegisterMessageResponse)
        {
            RegisterMessageResponse message = (RegisterMessageResponse) o;
            Secret = message.KeyPair;

        }else if(o instanceof EncryptedMessage){
            EncryptedMessage message = (EncryptedMessage) o;
            System.out.println("RECIEVER TODO: Decrypt message with stored secret, I§m too lazy. Encrypted message: " + message.Message);
        }
        
    }
}


//// MESSAGES
class InitMessage implements Serializable {
    public final ActorRef r;

    public InitMessage(ActorRef r) {
        this.r = r;
    }
}

class RegisterMessage implements Serializable{
}

class RegisterMessageResponse implements Serializable{
    public final KeyPair KeyPair;
    
    public RegisterMessageResponse(KeyPair kp) {
        this.KeyPair = kp;
    }
}

class LookUpMessage implements Serializable{
    public final ActorRef Actor;
    
    public LookUpMessage(ActorRef actor) {
        this.Actor = actor;
    }
}

class PublicKeyResponseMessage implements Serializable{
    public final int Public_key;
    public final ActorRef Actor;
    
    public PublicKeyResponseMessage(int pk, ActorRef actor) {
        this.Public_key = pk;
        this.Actor = actor;
    }
}

class CommunicationMessage implements Serializable{
    public final ActorRef ActorToLookup;
    
    public CommunicationMessage(ActorRef ar) {
        this.ActorToLookup = ar;
    }
}

class EncryptedMessage implements Serializable{
    public final String Message;
    
    public EncryptedMessage(String message) {
        this.Message = message;
    }
}

//// MAIN
public class SecSystem {
    public static void main(String[] args) {
        // Spawning actors
        final ActorSystem system = ActorSystem.create("SecSystem");
        final ActorRef registryActor = system.actorOf(Props.create(RegistryActor.class), "registryActor");
        final ActorRef senderActor = system.actorOf(Props.create(SenderActor.class), "senderActor");
        final ActorRef receiverActor = system.actorOf(Props.create(RecieverActor.class), "receiverActor");

        // Initiating
        registryActor.tell(new InitMessage(registryActor),ActorRef.noSender());      
        receiverActor.tell(new InitMessage(registryActor),ActorRef.noSender());
        senderActor.tell(new InitMessage(registryActor),ActorRef.noSender());

        // Communication
        senderActor.tell(new CommunicationMessage(receiverActor),ActorRef.noSender());

    }
}

