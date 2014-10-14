import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The GUI class builds the shiTunes Graphical User Interface
 *
 * @author shiTunes inc.
 */
public class GUI extends JFrame{

    private MusicPlayer player;
    private ShiBase db;
    private int selectedSongIndex;

    // GUI Frame and contents
    private JFrame shiTunesFrame;
    private JPanel buttonPanel;
    private JTable libTable;
    JPanel mainPanel;
    JMenuBar menuBar;
    JPanel libraryPanel;
    JPopupMenu popup;

    // UI Components
    BufferedImage playResource;
    BufferedImage pauseResource;
    BufferedImage stopResource;
    BufferedImage previousResource;
    BufferedImage nextResource;

    ImageIcon stopIcon;
    ImageIcon pauseIcon;
    ImageIcon playIcon;
    ImageIcon previousIcon;
    ImageIcon nextIcon;

    JButton playButton;
    JButton pauseButton;
    JButton stopButton;
    JButton previousButton;
    JButton nextButton;

    // File Chooser
    private JFileChooser chooser = new JFileChooser();
    private static FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");

    /**
     * The GUI default constructor
     * <p>
     * Initializes and connects to the database (ShiBase),
     * Builds the shiBase GUI, including Music Library table,
     * Main Menu and buttons (previous, play/pause, stop, next)
     *
     */
    public GUI(ShiBase db, MusicPlayer player) {
        // assign db arg to GUI db object
        this.db = db;

        // TODO: REMOVE loadDummyData() IN PRODUCTION CODE? (or keep it for demo)
        loadDummyData();

        // assign player arg to GUI player object
        this.player = player;

        // GUI initialization
        shiTunesFrame = new JFrame();
        shiTunesFrame.setTitle("shiTunes");
        shiTunesFrame.setMinimumSize(new Dimension(900,600));
        shiTunesFrame.setLocationRelativeTo(null);
        shiTunesFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Panel initialization
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        buttonPanel = new JPanel();
        libraryPanel = new JPanel(new GridLayout(1,1));

        // Menu bar initialization
        menuBar = new JMenuBar();
        menuBar.add(createFileMenu());



        // Build library table
        // instance library table model - this prevents individual cells from being editable
        DefaultTableModel tableModel = new DefaultTableModel(db.getAllSongs(), ShiBase.MUSIC_COLUMNS) {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };

        // Instantiate library table based off table model
        libTable = new JTable(tableModel);

        //Popup menu initialization
        createPopupMenu();

        // Instantiate scroll pane for library
        JScrollPane scrollPane = new JScrollPane(libTable);
        libTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        libTable.setFillsViewportHeight(true);

        // Set drop target on scroll table
        // enabling drag and drop of files into library
        scrollPane.setDropTarget(new DropTarget(){
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Transferable t = dtde.getTransferable();
                List fileList = null;
                String filepath;
                try {
                    fileList = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                    for(int i = 0; i < fileList.size(); i++) {
                        Song song = new Song(fileList.get(i).toString());
                        addSongToLibrary(song);
                    }
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Add scroll pane (library table) to library panel
        libraryPanel.add(scrollPane);

        // Setup Library table listener for selected row
        libTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                // Store selected song row index for use in skipping and getting selected song
                selectedSongIndex = libTable.getSelectedRow();
            }
        });


        libTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    playSong();
                }
            }
        });

        // Add UI components (buttons)
        addUIComponents();

        // Build main panel
        mainPanel.add(buttonPanel);
        mainPanel.add(libraryPanel);

        // Add all GUI components to shiTunes application frame
        shiTunesFrame.setJMenuBar(menuBar);
        shiTunesFrame.setContentPane(mainPanel);
        shiTunesFrame.pack();
        shiTunesFrame.setLocationByPlatform(true);
    }

    /**
     * Displays the shiTunes GUI
     *
     */
    public void displayGUI()
    {
        shiTunesFrame.setVisible(true);
    }

    /**
     * Adds UI Components to shiTunes GUI Panel
     *
     */
    private void addUIComponents() {
        try {
            // Initialize resources
            playResource = ImageIO.read(getClass().getResourceAsStream("/images/play.png"));
            pauseResource = ImageIO.read(getClass().getResourceAsStream("/images/pause.png"));
            stopResource = ImageIO.read(getClass().getResourceAsStream("/images/stop.png"));
            previousResource = ImageIO.read(getClass().getResourceAsStream("/images/previous.png"));
            nextResource = ImageIO.read(getClass().getResourceAsStream("/images/next.png"));
            stopIcon = new ImageIcon(stopResource);
            pauseIcon = new ImageIcon(pauseResource);
            playIcon = new ImageIcon(playResource);
            previousIcon = new ImageIcon(previousResource);
            nextIcon = new ImageIcon(nextResource);

            // Initialize buttons (toggle play/pause, stop, previous, next)
            // Setting icon during intialization
            playButton = new JButton(playIcon);
            pauseButton = new JButton(pauseIcon);
            stopButton = new JButton(stopIcon);
            previousButton = new JButton(previousIcon);
            nextButton = new JButton(nextIcon);

            // Set preferred button size
            playButton.setPreferredSize(new Dimension(40, 40));
            pauseButton.setPreferredSize(new Dimension(40, 40));
            stopButton.setPreferredSize(new Dimension(40, 40));
            previousButton.setPreferredSize(new Dimension(40, 40));
            nextButton.setPreferredSize(new Dimension(40, 40));

            // Set action listener for play button
            playButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                        playSong();
                }
            });

            // Set action listener for pause button
            pauseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("song index: " + selectedSongIndex);
                    System.out.println("getsongiflenamebyindex(selectedsongindex)" + getSongFilenameByIndex(selectedSongIndex));
                    if(getSongFilenameByIndex(selectedSongIndex) != null
                            && !getSongFilenameByIndex(selectedSongIndex).isEmpty()) {
                        if(player.getState() == 2 || player.getState() == 5) {
                            // player.state == playing/resumed
                            player.pause();
                        }
                    }
                }
            });

            // Set action listener for stop button
            stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("song index: " + selectedSongIndex);
                    player.stop();
                }
            });

            // Set action listener for previous button
            previousButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("song index: " + selectedSongIndex);
                    if(selectedSongIndex == 0) {
                        // at top of libTable, do nothing
                    } else {
                        if(player.getState() == 2 || player.getState() == 5) {
                            // if player is currently playing/resumed
                            // stop current song
                            // decriment selectedSongIndex
                            // play previous song
                            player.stop();
                            if(selectedSongIndex > 0) {
                                selectedSongIndex--;
                            }
                            player.play(getSongFilenameByIndex(selectedSongIndex));
                        }
                    }
                }
            });

            // Set action listener for next button
            nextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("song index: " + selectedSongIndex);
                    if(selectedSongIndex < libTable.getRowCount() - 1) {
                        if(player.getState() == 2 || player.getState() == 5) {
                            // if player is currently playing/resumed
                            // stop current song
                            // decriment selectedSongIndex
                            // play next song
                            player.stop();
                            selectedSongIndex++;
                            player.play(getSongFilenameByIndex(selectedSongIndex));
                        }
                    }
                }
            });

            // Add buttons to buttonPanel
            buttonPanel.add(previousButton);
            buttonPanel.add(playButton);
            buttonPanel.add(pauseButton);
            buttonPanel.add(stopButton);
            buttonPanel.add(nextButton);

            buttonPanel.setMaximumSize(new Dimension(1080, 40));
        } catch (IOException e) {
            // IOException thrown while reading resource files
            e.printStackTrace();
        }
    }

    /**
     * Plays the selected song
     */
    private void playSong() {
        String songFilePath = getSongFilenameByIndex(selectedSongIndex);
        boolean selectedSongExists = songFilePath != null && !songFilePath.isEmpty();
        boolean selectedSongIsCurrent = getSongFilenameByIndex(selectedSongIndex).equals(player.getCurrentSong());
        int playerState = player.getState();

        if (selectedSongExists) {
            // if there is a selected song
            if (selectedSongIsCurrent && playerState == 4) {
                // if selected song is current song on player
                // and player.state == paused
                player.resume();
            } else if (playerState == 3 || playerState == 0) {
                // otherwise, play selected song
                // if player.state == stopped
                // or player.state == opening (initial state before any song has played)
                player.play(songFilePath);
            } else if (!selectedSongIsCurrent) {
                player.stop();
                player.play(songFilePath);
            } else if (playerState == 2 || playerState == 5) {
                // player.state == playing/resumed
                // stop and then play song from beginning
                player.stop();
                player.play(songFilePath);
            }
        }
    }

    /**
     * Adds the given song to the library list and database
     *
     * @param song the song to add to the library/database
     */
    private void addSongToLibrary(Song song) {
        if(db.insertSong(song)) {
            // insert song was successful
            // Add row to JTable
            DefaultTableModel model = (DefaultTableModel) libTable.getModel();
            model.addRow(new Object[]{song.getArtist(), song.getTitle(), song.getAlbum(),
                    song.getYear(), song.getGenre(), song.getFilePath()});

        } else {
            // TODO: display something that tells the user the song isn't being added
        }
    }

    /**
     * Gets the song filename based on song library index
     *
     * @return the song filename based on song library index
     */
    private String getSongFilenameByIndex(int index) {
        // Ensure index is within bounds
        if(index >= 0 && index < libTable.getRowCount()) {
            return libTable.getValueAt(index, 5).toString();
        } else {
            return null;
        }
    }

    /**
     * Creates shiTunes file menu
     *
     * @return the shiTunes file menu
     */
    public JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem addItem = new JMenuItem("Add Song");
        JMenuItem deleteItem = new JMenuItem("Delete Song");
        JMenuItem exitItem = new JMenuItem("Exit");

        ActionListener openListener = new OpenItemListener();
        ActionListener addListener = new AddItemListener();
        ActionListener deleteListener = new DeleteItemListener();
        ActionListener exitListener = new ExitItemListener();

        openItem.addActionListener(openListener);
        addItem.addActionListener(addListener);
        deleteItem.addActionListener(deleteListener);
        exitItem.addActionListener(exitListener);

        menu.add(openItem);
        menu.add(addItem);
        menu.add(deleteItem);
        menu.add(exitItem);
        return menu;
    }

    /**
     * Exit item listener for the Main Menu.
     * <p>
     * When "Exit" is selected from the main menu the database connection
     * is closed and the shiTunes program exits gracefully
     *
     */
    class ExitItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            db.close();
            System.exit(0);
        }
    }

    /**
     * Open item listener for the Main Menu
     * <p>
     * When "Open" is selected from the main menu,
     * a file chooser opens and whichever song is selected
     * is added to the Music Library (temporarily) and played
     * <p>
     * Note: this is different from the "Add Song" command in that
     * the song is not added to the ShiBase database
     */
    class OpenItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            chooser.setFileFilter(filter);  //filters for mp3 files only
            //file chooser menu
            if (chooser.showOpenDialog(shiTunesFrame) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Song selectedSong = new Song(selectedFile.getPath());
                DefaultTableModel model = (DefaultTableModel) libTable.getModel();
                model.addRow(new Object[]{selectedSong.getArtist(), selectedSong.getTitle(), selectedSong.getAlbum(),
                        selectedSong.getYear(), selectedSong.getGenre(), selectedSong.getFilePath()});
                selectedSongIndex = model.getRowCount() - 1;
                player.play(selectedSong.getFilePath());
            }
        }
    }

    /**
     * Add item listener for the Main Menu
     * <p>
     * When "Add Song" is selected from the main menu
     * it is added to the database, if there is an error
     * when adding the song to the database (such as the song
     * already existing)
     */
    class AddItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            chooser.setFileFilter(filter);  //filters for mp3 files only
            //file chooser menu
            if (chooser.showOpenDialog(shiTunesFrame) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Song selectedSong = new Song(selectedFile.getPath());
                addSongToLibrary(selectedSong);
            }
        }
    }

    /**
     * Delete item listener for the Main Menu
     * <p>
     * When "Delete Song" is selected from the main menu
     * the selected song is deleted from the database
     * and removed from the Music Library listing
     *
     */
    class DeleteItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            // Get selected row to be deleted
            int row = libTable.getSelectedRow();

            // Stop player if song being deleted is the current song on the player
            if(getSongFilenameByIndex(row).equals(player.getCurrentSong())) {
                player.stop();
            }

            DefaultTableModel model = (DefaultTableModel) libTable.getModel();
            String selected = model.getValueAt(row, 5).toString();  // filepath of song @row
            model.removeRow(row);

            //Delete song from database by using filepath as an identifier
            db.deleteSong(selected);
        }
    }

    /**
     * Initializes popup menu for file addition/deletion
     * <p>
     * When user right clicks anywhere on JTable
     * menu items for delete and add popup.
     *
     *
     */
    private void createPopupMenu() {
        popup = new JPopupMenu();

        JMenuItem popupAdd = new JMenuItem("Add Song");
        JMenuItem popupDelete = new JMenuItem("Delete Song");
        ActionListener addListener = new AddItemListener();
        ActionListener deleteListener = new DeleteItemListener();
        popupAdd.addActionListener(addListener);
        popupDelete.addActionListener(deleteListener);

        MouseListener popupListen = new PopupListener();
        libTable.addMouseListener(popupListen);

        popup.add(popupAdd);
        popup.add(popupDelete);
    }

    /**
     * Popup listener for the right click menu
     * <p>
     * Interprets right mouse click to trigger
     * showing the popup menu
     *
     *
     */
    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /* TODO: Delete this in production code */
    private void loadDummyData() {
        String music_dir = getClass().getResource("/mp3/").getPath();
        Song song = new Song(music_dir + "1.mp3");
        db.insertSong(song);
        song = new Song(music_dir + "2.mp3");
        db.insertSong(song);
        song = new Song(music_dir + "3.mp3");
        db.insertSong(song);
        song = new Song(music_dir + "4.mp3");
        db.insertSong(song);
    }
}