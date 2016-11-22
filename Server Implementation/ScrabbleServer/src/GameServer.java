import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;

public class GameServer
{
    private static ServerSocket serverSocket = null;
    private static int clientCount = 0;
    private static int spectatorCount = 0;
    private static ClientThread [] clientThreads = new ClientThread[];
    private static Socket clientSocket = null;
    private static ArrayList<SpectatorThread> spectatorThreads = new ArrayList<>(Collections.nCopies(10, null));
    Board gameBoard;
    HashMap<Character, Integer> letterPoints;
    HashMap<Integer, String> playerData;
    Bag bag;
    DictSearch dictionary;

    public static void main(String [] args)
    {
        int portNumber = 7777;

        try
        {
            serverSocket = new ServerSocket(portNumber);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

        while(true)
        {
            //try to add as player, otherwise add to spectator arraylist
            try
            {
                clientSocket = serverSocket.accept();
                if(clientCount != 4 )
                {
                    for(int i = 0; i < clientThreads.length; i++)
                    {
                        if(clientThreads[i] == null)
                        {
                            clientThreads[i] = new ClientThread(clientSocket, clientThreads);
                            Thread t = new Thread(clientThreads[i]);
                            t.start();
                            clientCount++;
                            System.out.println("Clients " + clientCount);
                            break;
                        }
                    }
                }
                else
                {
                    int size = spectatorThreads.size();
                    for(int i = 0; i < size; i++)
                    {
                        if(spectatorThreads.get(i) == null)
                        {
                            spectatorThreads.set(i, new SpectatorThread(clientSocket, spectatorThreads));
                            Thread t = new Thread(spectatorThreads.get(i));
                            t.start();
                            spectatorCount++;
                            System.out.println("Spectators " + spectatorCount);
                            break;
                        }
                    }
                }
            }
            catch(IOException e)
            {
                System.out.println(e);
            }
        }
    }

    public GameServer()
    {
        //instantiate and start the server using the
        //specified amount of client sockets

        bag = new Bag();
        DictSearch dictionary = new DictSearch();
        assignTilePoints();
    }

    private void assignTilePoints()
    {
        letterPoints.put('A',1);
        letterPoints.put('B',3);
        letterPoints.put('C',3);
        letterPoints.put('D',2);
        letterPoints.put('E',1);
        letterPoints.put('F',4);
        letterPoints.put('G',2); 
        letterPoints.put('H',4);
        letterPoints.put('I',1);
        letterPoints.put('J',8);
        letterPoints.put('K',5);
        letterPoints.put('L',1);
        letterPoints.put('M',3);
        letterPoints.put('N',1);
        letterPoints.put('O',1);
        letterPoints.put('P',3);
        letterPoints.put('Q',10);
        letterPoints.put('R',1);
        letterPoints.put('S',1);
        letterPoints.put('T',1);
        letterPoints.put('U',1);
        letterPoints.put('V',4);
        letterPoints.put('W',4);
        letterPoints.put('X',8);
        letterPoints.put('Y',4);
        letterPoints.put('Z',10);
        letterPoints.put(' ',0);
    }

    public String encodeData(String input)
    {
        //Encode the data to XML for transport
        return input;
    }

    public String decodeData(String input)
    {
        //decode xml for use in the game
        return input;
    }

    public boolean sendData(Socket destination, String data)
    {
        //send the specied socket the data required
        return true;
    }

    public void updatePlayer(int player, String data)
    {
        //update specified player with specified data
    }

    public void updateBoard(CellSetter[] letterArray)
    {
        //Update the board with the specified data

        for(int i = 0; i < letterArray.length; i++)
        {
            char c = letterArray[i].character;
            gameBoard.setCells(letterArray[i].position, new Tile(c, letterPoints.get(c)));
        }
    }

    public boolean validateMove(String word)
    {
        //check if the word is legal
        return dictionary.search(word);
    }

    public void receiveData(String data)
    {
        this.data = data;
        

    }

    public boolean checkConnections()
    {
        //check if there is a current game in progress
        return true;
    }
}

class ClientThread implements Runnable
{
    private BufferedReader inputStream = null;
    private PrintStream outputStream = null;
    private Socket clientSocket = null;
    private ClientThread [] threads = new ClientThread[];
    private String name;
    private String role;
    //role signifier
    public ClientThread(Socket clientSocket, ClientThread [] threads)
    {
        this.clientSocket = clientSocket;
        this.threads = threads;
        try
        {
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            this.name = inputStream.readLine();
            this.role = "player";
            this.outputStream.println(role);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    public void run()
    {
        int maxClientCount = 4;
        ClientThread [] threads = new ClientThread[maxClientCount];

        try
        {
            for(int i = 0; i < maxClientCount; i++)
            {
                if(threads[i] != null)
                {
                    threads[i].outputStream.println(name + " just joined the game...");
                    //joining game logic here.
                }
            }

            while(true)
            {
                String line = inputStream.readLine();
                if(line.equals(role))
                {
                    break;
                }
                else
                {
                    for(int i = 0; i < maxClientCount; i++)
                    {
                        if(threads[i] != null && threads[i] != this)
                        {
                            //get move from players
                            String data = inputStream.readLine();
                            recieveData(data);
                        }
                    }
                }
            }

            for(int i = 0; i < maxClientCount; i++)
            {
                if(threads[i] != null && threads[i] != this)
                {
                    threads[i].outputStream.println(name + "has just left the game...");

                }
            }

            for(int i = 0; i < maxClientCount; i++)
            {
                if(threads[i] == this)
                {
                    threads[i] = null;
                }
            }

        }

        catch(IOException e)
        {

        }


    }



}
class SpectatorThread implements Runnable
{
    private BufferedReader inputStream = null;
    private PrintStream outputStream = null;
    private Socket clientSocket = null;
    private final  ArrayList<SpectatorThread> threads;
    private int maxClientsCount;
    private String name;
    private String role;

    public SpectatorThread(Socket clientSocket, ArrayList<SpectatorThread> threads)
    {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.size();
        try
        {
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            this.name = inputStream.readLine();
            this.role = "spectator";
            this.outputStream.println(role);
        }
        catch (IOException e)
        {

        }
    }

    public void run()
    {
        int maxClientsCount = this.maxClientsCount;
        ArrayList<SpectatorThread> threads = this.threads;

        try
        {

            for (int i = 0; i < maxClientsCount; i++)
            {
                if (threads.get(i) != null)
                {
                    threads.get(i).outputStream.println(name + " is spectating the game");
                }
            }
            while (true)
            {
                String line = inputStream.readLine();
                if (line.equals(role))
                {
                    break;
                }
                else
                {
                    for (int i = 0; i < maxClientsCount; i++)
                    {
                        if (threads.get(i) != null && threads.get(i) != this)
                        {
                            //redundant
                        }
                    }
                }
            }

            for (int i = 0; i < maxClientsCount; i++)
            {
                if (threads.get(i) != null && threads.get(i) != this)
                {
                    threads.get(i).outputStream.println(name + " has just left the game...");
                }
            }

            //Remove the users thread
            for (int i = 0; i < maxClientsCount; i++)
            {
                if (threads.get(i) == this)
                {
                    threads.set(i,null);
                }
            }

            //Close all streams when done
            inputStream.close();
            outputStream.close();
            clientSocket.close();
        }
        catch (IOException e)
        {

        }
    }
}