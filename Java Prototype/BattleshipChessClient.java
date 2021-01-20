package battleshipchessclient;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

/**
 *
 * @author Steven
 */
public class BattleshipChessClient {
    JFrame frame=new JFrame();
    JButton[][] buttons=new JButton[8][8];
    Piece[][] pieces=new Piece[8][8];
    DataInputStream din;
    DataOutputStream dout;
    
    JTextArea history=new JTextArea("");
    
    int player;
    int turn=1;
    Piece pieceToMove=null;
    final int PAWN=0,ROOK=1,KNIGHT=2,BISHOP=3,QUEEN=4,KING=5;
    int[] dead=new int[6];
    int ops=0;
    boolean gameEnd=false;
    
    Border black=BorderFactory.createLineBorder(Color.black);
    Border yellow=BorderFactory.createLineBorder(Color.yellow, 3);
    Border red=BorderFactory.createLineBorder(Color.red,3);
    
    class Piece {
        int type=0;
        int p=1;
        int x=-1;
        int y=-1;
        boolean pawnHasMoved=false;
        Piece(int i) {
          type=i;
        }
        @Override
        public String toString() {
          switch (type) {
              case ROOK:return "ROOK";
              case KNIGHT:return "KNIGHT";
              case BISHOP:return "BISHOP";
              case QUEEN:return "QUEEN";
              case KING:return "KING";
              default:return "PAWN";
          }
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        BattleshipChessClient test=new BattleshipChessClient();
        test.run();
    }
    public void run() throws IOException {
      Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
      ArrayList<String> ilist=new ArrayList<>();
      Collections.list(nets).stream().forEach((netint) -> {
          Enumeration<InetAddress> inetAddresses=netint.getInetAddresses();
          List<InetAddress> inetList=Collections.list(inetAddresses);
          if (inetList.size()>0) {
              for (int i=0; i<inetList.size(); i++) {
                  ilist.add(inetList.get(i).toString());
              }
          }
      });
      String choice=(String)JOptionPane.showInputDialog(null, "Choose an address", "Internet Addresses", JOptionPane.QUESTION_MESSAGE, null,ilist.toArray(),ilist.toArray()[0]);
      InetAddress inet=InetAddress.getByName(choice.substring(1));
      Socket conn = new Socket(inet, 2000);
      din=new DataInputStream(conn.getInputStream());
      dout=new DataOutputStream(conn.getOutputStream());
      player=din.readInt();
      buildFrame();
      setPieces();
      frame.setVisible(true);
      buildRecordFrame();
      while (!gameEnd) {
        String str=din.readUTF();
        if (str.equals("win")) {
          JOptionPane.showMessageDialog(null,"The enemy took your king. You have lost");
          for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
              if (pieces[i][j]!=null && pieces[i][j].p!=player) {
                buttons[i][j].setForeground(Color.red);
                buttons[i][j].setText(pieces[i][j].toString());
              }
            }
          }
        }
        if (str.contains("start")) {
          int x1=Character.digit(str.charAt(6),10);
          int y1=Character.digit(str.charAt(8),10);
          int x2=Character.digit(str.charAt(10),10);
          int y2=Character.digit(str.charAt(12),10);
          if (pieces[x2][y2]!=null && pieces[x2][y2].p==player) {
            dead[pieces[x2][y2].type]=dead[pieces[x2][y2].type]+1;
            ops++;
          }
          history.setText("-\n"+history.getText());
          if (str.contains("died")) {
            history.setText("Player "+(3-player)+" killed their own pawn lol\n"+history.getText());
            pieces[x1][y1]=null;
            turn=player;
            frame.setTitle("Your Turn");
            continue;
          } else {
            history.setText("Player "+(3-player)+" moved "+pieces[x1][y1].toString()+"\n"+history.getText());
            if (pieces[x2][y2]!=null && pieces[x2][y2].p==player) {
              history.setText(pieces[x2][y2].toString()+" died at "+x2+" "+y2+"\n"+history.getText());
            }
          }
          buttons[x2][y2].setText("");
          Piece temp=pieces[x1][y1];
          pieces[x1][y1]=null;
          pieces[x2][y2]=temp;
          pieces[x2][y2].x=x2;
          pieces[x2][y2].y=y2;
          turn=player;
          JOptionPane.showMessageDialog(null,"Your Turn");
          frame.setTitle("Your Turn");
        }
        if (str.contains("set")) {
          int x=Character.digit(str.charAt(4),10);
          int y=Character.digit(str.charAt(6),10);
          int t=Character.digit(str.charAt(8),10);
          pieces[x][y].type=t;
        }
      }
    }
    public void buildRecordFrame() {
      JFrame rframe=new JFrame("History");
      rframe.setSize(300,500);
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
      Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
      int x = (int) rect.getMaxX()-rframe.getWidth();
      rframe.setLocation(x, 0);
      JPanel panel=new JPanel();
      panel.setLayout(null);
      panel.add(history);
      history.setBounds(10,10,265,435);
      history.setEditable(false);
      rframe.add(panel);
      rframe.setVisible(true);
    }
    public void buildFrame() {
      if (player==2) {
        frame.setTitle("Their Turn");
      } else {
        frame.setTitle("Your Turn");
      }
      frame.setSize(500,500);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JPanel panel=new JPanel();
      panel.setLayout(new GridLayout(8,8));
      for (int i=0; i<buttons.length; i++) {
        for (int j=0; j<buttons[i].length; j++) {
          buttons[i][j]=new JButton();
          buttons[i][j].setBorder(black);
          if ((i+j)%2==0) {
            buttons[i][j].setBackground(Color.white);
            buttons[i][j].setForeground(Color.black);
          } else {
            buttons[i][j].setBackground(Color.black);
            buttons[i][j].setForeground(Color.white);
          }
          buttons[i][j].addActionListener(new ButtonAction(i,j));
          panel.add(buttons[i][j]);
        }
      }
      frame.add(panel);
    }
    
    public void setPieces() {
      int eplayer=3-player;
      addPiece(ROOK,player,0,0);
      addPiece(KNIGHT,player,0,1);
      addPiece(BISHOP,player,0,2);
      if (player==2) {
        addPiece(QUEEN,player,0,3);
        addPiece(KING,player,0,4);
      } else {
        addPiece(KING,player,0,3);
        addPiece(QUEEN,player,0,4);
      }
      addPiece(BISHOP,player,0,5);
      addPiece(KNIGHT,player,0,6);
      addPiece(ROOK,player,0,7);
      for (int i=0; i<8; i++) {
        addPiece(PAWN,player,1,i);
      }
      
      addPiece(ROOK,eplayer,7,0);
      addPiece(KNIGHT,eplayer,7,1);
      addPiece(BISHOP,eplayer,7,2);
      if (player==2) {
        addPiece(QUEEN,eplayer,7,3);
        addPiece(KING,eplayer,7,4);
      } else {
        addPiece(KING,eplayer,7,3);
        addPiece(QUEEN,eplayer,7,4);
      }
      addPiece(BISHOP,eplayer,7,5);
      addPiece(KNIGHT,eplayer,7,6);
      addPiece(ROOK,eplayer,7,7);
      for (int i=0; i<8; i++) {
        addPiece(PAWN,eplayer,6,i);
      }      
    }
    public void addPiece(int pnum, int plr, int i, int j) {
      Piece pc=new Piece(pnum);
      pc.p=plr;
      pc.x=i;
      pc.y=j;
      pieces[i][j]=pc;
      if (pc.p==player) {
        buttons[i][j].setText(pc.toString());
      }
    }
    class ButtonAction implements ActionListener {
      int x,y;
      ButtonAction(int i, int j) {
        this.x=i;
        this.y=j;
      }
      @Override
      public void actionPerformed(ActionEvent e) {
        if (turn==player && pieces[x][y]!=null && pieces[x][y].p==player && pieceToMove==null) {
          buttons[x][y].setBorder(yellow);
          pieceToMove=pieces[x][y];
          switch (pieces[x][y].type) {
              case PAWN:
                  if (x<7 && y>0  && (pieces[x+1][y-1]==null || pieces[x+1][y-1].p!=player))
                    buttons[x+1][y-1].setBorder(red);
                  if (x<7 && (pieces[x+1][y]==null || pieces[x+1][y].p!=player))
                    buttons[x+1][y].setBorder(yellow);
                  if (x<7 && y<7 && (pieces[x+1][y+1]==null || pieces[x+1][y+1].p!=player))
                    buttons[x+1][y+1].setBorder(red);
                  if (!pieces[x][y].pawnHasMoved && (pieces[x+1][y]==null || pieces[x+1][y].p!=player)
                          && (pieces[x+2][y]==null || pieces[x+2][y].p!=player)) {
                    buttons[x+2][y].setBorder(yellow);
                  }
                  break;
              case ROOK:
                  for (int i=x+1; i<8; i++) {
                    if (pieces[i][y]!=null) {
                      if (pieces[i][y].p==player) {
                        break;
                      }
                    }
                    buttons[i][y].setBorder(yellow);
                  }
                  for (int i=x-1; i>=0; i--) {
                    if (pieces[i][y]!=null) {
                      if (pieces[i][y].p==player) {
                        break;
                      }
                    }
                    buttons[i][y].setBorder(yellow);
                  }
                  for (int i=y+1; i<8; i++) {
                    if (pieces[x][i]!=null) {
                      if (pieces[x][i].p==player) {
                        break;
                      }
                    }
                    buttons[x][i].setBorder(yellow);
                  }
                  for (int i=y-1; i>=0; i--) {
                    if (pieces[x][i]!=null) {
                      if (pieces[x][i].p==player) {
                        break;
                      }
                    }
                    buttons[x][i].setBorder(yellow);
                  }
                  break;
              case KNIGHT:
                  if (x>1) {
                    if (y>0) {
                      if (pieces[x-2][y-1]==null || pieces[x-2][y-1].p!=player) {
                        buttons[x-2][y-1].setBorder(yellow);
                      }
                    }
                    if (y<7) {
                      if (pieces[x-2][y+1]==null || pieces[x-2][y+1].p!=player) {
                        buttons[x-2][y+1].setBorder(yellow);
                      }
                    }
                  }
                  if (x>0) {
                    if (y>1) {
                      if (pieces[x-1][y-2]==null || pieces[x-1][y-2].p!=player) {
                        buttons[x-1][y-2].setBorder(yellow);
                      }
                    }
                    if (y<6) {
                      if (pieces[x-1][y+2]==null || pieces[x-1][y+2].p!=player) {
                        buttons[x-1][y+2].setBorder(yellow);
                      }
                    }
                  }
                  if (x<6) {
                    if (y>0) {
                      if (pieces[x+2][y-1]==null || pieces[x+2][y-1].p!=player) {
                        buttons[x+2][y-1].setBorder(yellow);
                      }
                    }
                    if (y<7) {
                      if (pieces[x+2][y+1]==null || pieces[x+2][y+1].p!=player) {
                        buttons[x+2][y+1].setBorder(yellow);
                      }
                    }
                  }
                  if (x<7) {
                    if (y>1) {
                      if (pieces[x+1][y-2]==null || pieces[x+1][y-2].p!=player) {
                        buttons[x+1][y-2].setBorder(yellow);
                      }
                    }
                    if (y<6) {
                      if (pieces[x+1][y+2]==null || pieces[x+1][y+2].p!=player) {
                        buttons[x+1][y+2].setBorder(yellow);
                      }
                    }
                  }
                  break;
              case BISHOP:
                  int i=x+1;
                  int j=y+1;
                  while (i<8 && j<8) {
                    if (pieces[i][j]!=null) {
                      if (pieces[i][j].p==player) {
                        break;
                      }
                    }
                    buttons[i][j].setBorder(yellow);
                    i++;
                    j++;
                  }
                  i=x-1;
                  j=y+1;
                  while (i>=0 && j<8) {
                    if (pieces[i][j]!=null) {
                      if (pieces[i][j].p==player) {
                        break;
                      }
                    }
                    buttons[i][j].setBorder(yellow);
                    i--;
                    j++;
                  }
                  i=x+1;
                  j=y-1;
                  while (i<8 && j>=0) {
                    if (pieces[i][j]!=null) {
                      if (pieces[i][j].p==player) {
                        break;
                      }
                    }
                    buttons[i][j].setBorder(yellow);
                    i++;
                    j--;
                  }
                  i=x-1;
                  j=y-1;
                  while (i>=0 && j>=0) {
                    if (pieces[i][j]!=null) {
                      if (pieces[i][j].p==player) {
                        break;
                      }
                    }
                    buttons[i][j].setBorder(yellow);
                    i--;
                    j--;
                  }
                  break;
              case QUEEN:
                  for (i=x+1; i<8; i++) {
                    if (pieces[i][y]!=null) {
                      if (pieces[i][y].p==player) {
                        break;
                      }
                    }
                    buttons[i][y].setBorder(yellow);
                  }
                  for (i=x-1; i>=0; i--) {
                    if (pieces[i][y]!=null) {
                      if (pieces[i][y].p==player) {
                        break;
                      }
                    }
                    buttons[i][y].setBorder(yellow);
                  }
                  for (i=y+1; i<8; i++) {
                    if (pieces[x][i]!=null) {
                      if (pieces[x][i].p==player) {
                        break;
                      }
                    }
                    buttons[x][i].setBorder(yellow);
                  }
                  for (i=y-1; i>=0; i--) {
                    if (pieces[x][i]!=null) {
                      if (pieces[x][i].p==player) {
                        break;
                      }
                    }
                    buttons[x][i].setBorder(yellow);
                  }
                  i=x+1;
                  j=y+1;
                  while (i<8 && j<8) {
                    if (pieces[i][j]!=null) {
                      if (pieces[i][j].p==player) {
                        break;
                      }
                    }
                    buttons[i][j].setBorder(yellow);
                    i++;
                    j++;
                  }
                  i=x-1;
                  j=y+1;
                  while (i>=0 && j<8) {
                    if (pieces[i][j]!=null) {
                      if (pieces[i][j].p==player) {
                        break;
                      }
                    }
                    buttons[i][j].setBorder(yellow);
                    i--;
                    j++;
                  }
                  i=x+1;
                  j=y-1;
                  while (i<8 && j>=0) {
                    if (pieces[i][j]!=null) {
                      if (pieces[i][j].p==player) {
                        break;
                      }
                    }
                    buttons[i][j].setBorder(yellow);
                    i++;
                    j--;
                  }
                  i=x-1;
                  j=y-1;
                  while (i>=0 && j>=0) {
                    if (pieces[i][j]!=null) {
                      if (pieces[i][j].p==player) {
                        break;
                      }
                    }
                    buttons[i][j].setBorder(yellow);
                    i--;
                    j--;
                  }
                  break;
              case KING:
                  for (i=Math.max(0, x-1);i<=Math.min(7,x+1);i++) {
                    for (j=Math.max(0,y-1);j<=Math.min(7,y+1);j++) {
                      if (!(x==i && y==j) && (pieces[i][j]==null || pieces[i][j].p!=player)) {
                          buttons[i][j].setBorder(yellow);
                      }
                    }
                  }
                  break;
          }
        } else if (pieceToMove!=null && x==pieceToMove.x && y==pieceToMove.y) {
          for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
              buttons[i][j].setBorder(black);
            }
          }
          pieceToMove=null;
        } else if (!buttons[x][y].getBorder().equals(black)) {
          int realX=x;
          int realY=y;
          switch (pieceToMove.type) {
              case PAWN:
                  if (y!=pieceToMove.y && pieces[x][y]==null) {
                    for (int i=0; i<8; i++) {
                      for (int j=0; j<8; j++) {
                        buttons[i][j].setBorder(black);
                      }
                    }
                    history.setText("You failed to hit an enemy. The pawn dies.\n-\n"+history.getText());
                    buttons[pieceToMove.x][pieceToMove.y].setText("");
                    pieces[pieceToMove.x][pieceToMove.y]=null;
                    
                    turn=3-player;
                    frame.setTitle("Their Turn");
                    try {
                      dout.writeUTF("start "+pieceToMove.x+" "+pieceToMove.y+" "+x+" "+y+" died");
                    } catch (IOException ex) {}
                    pieceToMove=null;
                    return;
                  } else if (y==pieceToMove.y) {
                    realX=pieceToMove.x;
                    while (realX!=x && pieces[realX+1][y]==null) {
                      realX++;
                    }
                    int temp=x;
                    x=realX;
                    realX=temp;
                  }
                  if (x==7 && ops>0) {
                    String[] rezOps=new String[ops];
                    int j=0;
                    for (int i=0; i<6; i++) {
                      if (dead[i]>0) {
                        rezOps[j]=(new Piece(i)).toString();
                        j++;
                      }
                    }
                    String choice = (String)JOptionPane.showInputDialog(null,"Select your player:","Player Power",JOptionPane.QUESTION_MESSAGE,null,rezOps,rezOps[0]);
                    for (int i=0; i<6; i++) {
                      if ((new Piece(i)).toString().equals(choice)) {
                        pieceToMove.type=i;
                        buttons[pieceToMove.x][pieceToMove.y].setText(choice);
                      }
                    }
                    try {
                      dout.writeUTF("set "+pieceToMove.x+" "+pieceToMove.y+" "+pieceToMove.type);
                    } catch (IOException ex) {}
                  }
                  pieceToMove.pawnHasMoved=true;
                  break;
              case ROOK:
                  if (y==pieceToMove.y) {
                    int d=-(pieceToMove.x-x)/Math.abs(pieceToMove.x-x);
                    realX=pieceToMove.x+d;
                    while (realX!=x && pieces[realX][y]==null) {
                      realX+=d;
                    }
                    int temp=x;
                    x=realX;
                    realX=temp;
                  } else if (x==pieceToMove.x) {
                    int d=-(pieceToMove.y-y)/Math.abs(pieceToMove.y-y);
                    realY=pieceToMove.y+d;
                    while (realY!=y && pieces[x][realY]==null) {
                      realY+=d;
                    }
                    int temp=y;
                    y=realY;
                    realY=temp;
                  }
                  break;
              case BISHOP:
                  int dx=-(pieceToMove.x-x)/Math.abs(pieceToMove.x-x);
                  int dy=-(pieceToMove.y-y)/Math.abs(pieceToMove.y-y);
                  realX=pieceToMove.x+dx;
                  realY=pieceToMove.y+dy;
                  while (realX!=x && pieces[realX][realY]==null) {
                    realX+=dx;
                    realY+=dy;
                  }
                  int temp=x;
                  x=realX;
                  realX=temp;
                  temp=y;
                  y=realY;
                  realY=temp;
                  break;
              case QUEEN:
                  if (pieceToMove.x==x) {
                    dx=0;
                  } else {
                    dx=-(pieceToMove.x-x)/Math.abs(pieceToMove.x-x);
                  }
                  if (pieceToMove.y==y) {
                    dy=0;
                  } else {
                    dy=-(pieceToMove.y-y)/Math.abs(pieceToMove.y-y);
                  }
                  realX=pieceToMove.x+dx;
                  realY=pieceToMove.y+dy;
                  while ((realX!=x || realY!=y) && pieces[realX][realY]==null) {
                    realX+=dx;
                    realY+=dy;
                  }
                  temp=x;
                  x=realX;
                  realX=temp;
                  temp=y;
                  y=realY;
                  realY=temp;
                  break;
          }
          if (pieces[x][y]!=null && pieces[x][y].type==KING) {
            try {
              dout.writeUTF("win");
            } catch (IOException ex) {}
            for (int i=0; i<8; i++) {
              for (int j=0; j<8; j++) {
                if (pieces[i][j]!=null && pieces[i][j].p!=player && pieces[i][j].type!=KING) {
                  buttons[i][j].setForeground(Color.red);
                  buttons[i][j].setText(pieces[i][j].toString());
                }
              }
            }
            JOptionPane.showMessageDialog(null,"You win!");
          }
          try {
            dout.writeUTF("start "+pieceToMove.x+" "+pieceToMove.y+" "+x+" "+y);
          } catch (IOException ex) {}
          history.setText("You moved "+pieceToMove.toString()+"\n-\n"+history.getText());
          if (pieces[x][y]!=null && pieces[x][y].p!=player) {
            history.setText("You killed something.\n"+history.getText());
          }
          buttons[pieceToMove.x][pieceToMove.y].setText("");
          buttons[x][y].setText(pieceToMove.toString());
          pieces[pieceToMove.x][pieceToMove.y]=null;
          pieces[x][y]=pieceToMove;
          pieceToMove.x=x;
          pieceToMove.y=y;
          for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
              buttons[i][j].setBorder(black);
            }
          }
          pieceToMove=null;
          x=realX;
          y=realY;
          turn=3-player;
          frame.setTitle("Their Turn");
        }
      }
    }
}
