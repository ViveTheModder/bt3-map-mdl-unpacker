package gui;
//Budokai Tenkaichi 3 Map Model Unpacker by ViveTheModder
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import cmd.Main;

public class App 
{
	private static File src=null;
	private static RandomAccessFile map=null;
	private static final Font BOLD = new Font("Tahoma", 1, 24);
	private static final Font MED = new Font("Tahoma", 0, 18);
	private static final Font BOLD_S = new Font("Tahoma", 1, 12);
	private static final String HTML_A_START = "<html><a href=''>";
	private static final String HTML_A_END = "</a></html>";
	private static final String WINDOW_TITLE = "BT3 Map MDL Unpacker";
	private static final Toolkit DEF_TOOLKIT = Toolkit.getDefaultToolkit();
	private static final Image ICON = DEF_TOOLKIT.getImage(ClassLoader.getSystemResource("img/icon.png"));
	
	private static RandomAccessFile getMapFromChooser() throws IOException
	{
		src=null;
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter mdlFilter = new FileNameExtensionFilter("00_map_main_data.mdl (.MDL)", "mdl");
		FileNameExtensionFilter pakFilter = new FileNameExtensionFilter("Map File (.PAK, .UNK)", "pak", "unk");
		chooser.addChoosableFileFilter(mdlFilter);
		chooser.addChoosableFileFilter(pakFilter);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(pakFilter);
		chooser.setDialogTitle("Open File...");
		while (src==null)
		{
			int result = chooser.showOpenDialog(chooser);
			if (result==0) 
			{
				src = chooser.getSelectedFile();
				String fileName = src.getName().toLowerCase();
				String fileExt = fileName.replaceAll("^.*\\.(.*)$", "$1");
				return Main.getMap(src,fileExt);
			}
			else break;
		}
		return null;	
	}
	private static void errorBeep()
	{
		Runnable runWinErrorSnd = (Runnable) DEF_TOOLKIT.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd!=null) runWinErrorSnd.run();
	}
	private static void setApplication()
	{
		//initialize components
		Box btnBox = Box.createHorizontalBox();
		GridBagConstraints gbc = new GridBagConstraints();
		Image img = ICON.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
		ImageIcon imgIcon = new ImageIcon(img);
		JButton exportBtn = new JButton("Export...");
		JButton exportAllBtn = new JButton("Export All...");
		JComboBox<String> dropdown = new JComboBox<String>(Main.SECT_NAMES);
		JFrame frame = new JFrame();
		JLabel label = new JLabel("MDL Section To Export:");
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		JMenuItem open = new JMenuItem("Open File...");
		JPanel panel = new JPanel();
		//set component properties
		dropdown.setFont(MED);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		label.setFont(BOLD);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel.setLayout(new GridBagLayout());
		about.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Box[] boxes = new Box[3];
				Box mainBox = Box.createVerticalBox();
				String[] authorLinks = {"https://github.com/ViveTheModder","https://github.com/Vrass28","https://github.com/jagger1407"};
				String[] text = {"Made by: ","Research done by: ","Greatly inspired by: "};
				JLabel[] authors = 
				{
					new JLabel(HTML_A_START+"ViveTheModder"+HTML_A_END), new JLabel(HTML_A_START+"Vras (and others)"+HTML_A_END), 
					new JLabel(HTML_A_START+"jagger1407"+HTML_A_END)
				};
				for (int i=0; i<authors.length; i++)
				{
					final int index=i;
					boxes[i] = Box.createHorizontalBox();
					JLabel textLabel = new JLabel(text[i]);
					textLabel.setFont(BOLD_S);
					authors[i].setFont(BOLD_S);
					boxes[i].add(textLabel);
					boxes[i].add(authors[i]);					
					authors[i].addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							try {
								Desktop.getDesktop().browse(new URI(authorLinks[index]));
							} catch (IOException | URISyntaxException e1) {
								errorBeep();
								JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
							}
						}});
					mainBox.add(boxes[i]);
				}
				JOptionPane.showMessageDialog(null, mainBox, WINDOW_TITLE, 1, imgIcon);
			}
		});
		exportAllBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try {
					int msgType=0;
					String msg="No MDL or PAK file has been provided!";
					if (src!=null) 
					{
						if (map!=null)
						{
							long start = System.currentTimeMillis();
							Main.writeSectionToFile(src, map, -1);
							long finish = System.currentTimeMillis();
							double time = (finish-start)/1000.0;
							msg="All sections extracted successfully in "+time+" s!";
							msgType=1;
						}
						else msg="Invalid PAK file! Its MDL cannot be accessed either because of the wrong\n"
						+"number of entries or the file size not matching with the last entry.";
					}
					if (msgType==1) DEF_TOOLKIT.beep();
					else errorBeep();
					JOptionPane.showMessageDialog(null, msg, WINDOW_TITLE, msgType);
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
				}
			}
		});
		exportBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try {
					int msgType=0;
					String msg = "No MDL or PAK file has been provided!";
					if (src!=null)
					{
						if (map!=null)
						{
							long start = System.currentTimeMillis();
							Main.writeSectionToFile(src, map, dropdown.getSelectedIndex());
							long finish = System.currentTimeMillis();
							double time = (finish-start)/1000.0;
							msg=dropdown.getSelectedItem()+" extracted successfully in "+time+" s!";
							msgType=1;
						}
						else msg="Invalid PAK file! Its MDL cannot be accessed either because of the wrong\n"
						+"number of entries or the file size not matching with the last entry.";
					}
					if (msgType==1) DEF_TOOLKIT.beep();
					else errorBeep();
					JOptionPane.showMessageDialog(null, msg, WINDOW_TITLE, msgType);
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
				}			
			}
		});
		open.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try {
					map = getMapFromChooser();
					frame.setTitle(WINDOW_TITLE+" - "+src.getCanonicalPath());
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
				}
			}
		});
		//add components
		btnBox.add(exportBtn);
		btnBox.add(Box.createHorizontalStrut(20));
		btnBox.add(exportAllBtn);
		fileMenu.add(open);
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		panel.add(label,gbc);
		panel.add(dropdown,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(btnBox,gbc);
		//set frame properties
		frame.add(panel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setIconImage(ICON);
		frame.setJMenuBar(menuBar);
		frame.setLocationRelativeTo(null);
		frame.setSize(512,256);
		frame.setTitle(WINDOW_TITLE);
		frame.setVisible(true);
	}
	public static void main(String[] args) 
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setApplication();
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) 
		{
			e.printStackTrace();
		}
	}
}