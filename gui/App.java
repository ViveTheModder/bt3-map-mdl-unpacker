package gui;
//Tenkaichi Map Model Unpacker by ViveTheModder
import java.awt.Component;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
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
	public static int langIndex=0;
	public static final String[][] LANG_TEXT = 
	{
		{
			"MDL Section To Export:","File","Help","About","Open File...","Made by:","Research done by: ","Greatly inspired by: ",
			"Exception","No MDL or PAK file has been provided!","All sections extracted successfully in [time] s!",
			"Invalid PAK file! Its MDL cannot be accessed either because of the wrong number of entries or the file size not matching with the last entry.",
			"Export...","Export All..","extracted successfully in [time] s!","Language",
			"Destructible Objects' Coordinates","Map Model Data","Map Model Part Data",
			"Rendering Indices","Map Texture Container (DBT)","Rendering Distances",
			"Collision Data","Destructible Objects Data","Barrier Parameters",
			"Extracting [section] from [filename]...","Enter a valid path to a BT3 map's PAK, MDL or UNK file:",
			"Enter one of the numbers below representing the section to extract.","Time: [time] s"
		},
		new String[30],
		new String[30],
		new String[30]
	};
	private static File src=null;
	private static RandomAccessFile map=null;
	private static final Font BOLD = new Font("Tahoma", 1, 24);
	private static final Font MED = new Font("Tahoma", 0, 18);
	private static final Font BOLD_S = new Font("Tahoma", 1, 12);
	private static final String HTML_A_START = "<html><a href=''>";
	private static final String HTML_A_END = "</a></html>";
	private static final String WINDOW_TITLE = "Tenkaichi Map MDL Unpacker";
	private static final Toolkit DEF_TOOLKIT = Toolkit.getDefaultToolkit();
	private static final Image ICON = DEF_TOOLKIT.getImage(ClassLoader.getSystemResource("img/icon.png"));
	private static final String[][] LANG_NAMES = 
	{
		{"English","German","Italian","Portuguese (PT-BR)"},
		{"Englisch", "Deutsch", "Italienisch", "Portugiesisch (PT-BR)"},
		{"Inglese", "Tedesco", "Italiano", "Portoghese (PT-BR)"},
		{"Inglês", "Alemão", "Italiano", "Português (PT-BR)"}
	};
	
	public static void setLangText() throws IOException
	{
		File langFolder = new File("./lang/");
		File[] langFiles = langFolder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) 
			{
				return name.toLowerCase().startsWith("lang-") && name.toLowerCase().endsWith(".txt");
			}
		});
		for (File lang: langFiles)
		{
			String fileName = lang.getName();
			if (Integer.parseInt(fileName.split("-")[1])==langIndex)
			{
				if (LANG_TEXT[langIndex][0]==null)
				{
					int lineCnt=0;
					Scanner sc;
					if (Main.isGuiDisabled==true) sc = new Scanner(lang);
					else sc = new Scanner(lang,"ISO-8859-1");
					while (sc.hasNextLine())
					{
						LANG_TEXT[langIndex][lineCnt] = sc.nextLine();
						lineCnt++;
					}
					sc.close();
				}
			}
		}
	}
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
		chooser.setDialogTitle(LANG_TEXT[langIndex][4]);
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
		JButton exportBtn = new JButton(LANG_TEXT[langIndex][12]);
		JButton exportAllBtn = new JButton(LANG_TEXT[langIndex][13]);
		JComboBox<String> dropdown = new JComboBox<String>(Main.SECT_NAMES);
		JFrame frame = new JFrame();
		JLabel label = new JLabel(LANG_TEXT[langIndex][0]);
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu(LANG_TEXT[langIndex][1]);
		JMenu helpMenu = new JMenu(LANG_TEXT[langIndex][2]);
		JMenu langMenu = new JMenu(LANG_TEXT[langIndex][15]);
		JMenuItem about = new JMenuItem(LANG_TEXT[langIndex][3]);
		JMenuItem open = new JMenuItem(LANG_TEXT[langIndex][4]);
		JMenuItem[] langs = new JMenuItem[LANG_NAMES[0].length];
		JPanel panel = new JPanel();
		for (int i=0; i<LANG_NAMES[0].length; i++) 
		{
			langs[i] = new JMenuItem(LANG_NAMES[langIndex][i]);
			langMenu.add(langs[i]);
		}
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
				String[] text = {LANG_TEXT[langIndex][5],LANG_TEXT[langIndex][6],LANG_TEXT[langIndex][7]};
				String[] translators = {"jagger1407","parmiboy","Nero_149"};
				JLabel[] authors = 
				{
					new JLabel(HTML_A_START+"ViveTheModder"+HTML_A_END), new JLabel(HTML_A_START+"Vras (et al)"+HTML_A_END), 
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
								JOptionPane.showMessageDialog(frame, e1.getClass().getName()+": "+e1.getLocalizedMessage(), LANG_TEXT[langIndex][8], 0);
							}
						}});
					mainBox.add(boxes[i]);
				}
				if (langIndex!=0) 
				{
					Box translatorBox = Box.createHorizontalBox();
					JLabel translatedBy = new JLabel(LANG_TEXT[langIndex][29]+" ");
					JLabel translator = new JLabel(translators[langIndex-1]);
					translatedBy.setFont(BOLD_S);
					translator.setFont(BOLD_S);
					translatorBox.add(translatedBy);
					translatorBox.add(translator);
					translatorBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
					mainBox.add(translatorBox);
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
					String msg=LANG_TEXT[langIndex][9];
					if (src!=null) 
					{
						if (map!=null)
						{
							long start = System.currentTimeMillis();
							Main.writeSectionToFile(src, map, -1);
							long finish = System.currentTimeMillis();
							double time = (finish-start)/1000.0;
							msg=LANG_TEXT[langIndex][10].replace("[time]", ""+time+"");
							msgType=1;
						}
						else 
						{
							String[] msgArr = LANG_TEXT[langIndex][11].split("&");
							msg=msgArr[0].replace("&", "")+"\n"+msgArr[1];
						}
					}
					if (msgType==1) DEF_TOOLKIT.beep();
					else errorBeep();
					JOptionPane.showMessageDialog(null, msg, WINDOW_TITLE, msgType);
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getLocalizedMessage(), LANG_TEXT[langIndex][8], 0);
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
					String msg = LANG_TEXT[langIndex][9];
					if (src!=null)
					{
						if (map!=null)
						{
							long start = System.currentTimeMillis();
							Main.writeSectionToFile(src, map, dropdown.getSelectedIndex());
							long finish = System.currentTimeMillis();
							double time = (finish-start)/1000.0;
							msg=dropdown.getSelectedItem()+" "+LANG_TEXT[langIndex][14].replace("[time]", ""+time+"");
							msgType=1;
						}
						else 
						{
							String[] msgArr = LANG_TEXT[langIndex][11].split("&");
							msg=msgArr[0].replace("&", "")+"\n"+msgArr[1];
						}
					}
					if (msgType==1) DEF_TOOLKIT.beep();
					else errorBeep();
					JOptionPane.showMessageDialog(null, msg, WINDOW_TITLE, msgType);
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getLocalizedMessage(), LANG_TEXT[langIndex][8], 0);
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
					if (src!=null) frame.setTitle(WINDOW_TITLE+" - "+src.getCanonicalPath());
					else frame.setTitle(WINDOW_TITLE);
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getLocalizedMessage(), LANG_TEXT[langIndex][8], 0);
				}
			}
		});
		for (int i=0; i<LANG_NAMES[0].length; i++)
		{
			final int index=i;
			langs[i].addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					try {
						langIndex=index;
						setLangText();
						//apply language text to visible components
						label.setText(LANG_TEXT[langIndex][0]);
						fileMenu.setText(LANG_TEXT[langIndex][1]);
						helpMenu.setText(LANG_TEXT[langIndex][2]);
						about.setText(LANG_TEXT[langIndex][3]);
						open.setText(LANG_TEXT[langIndex][4]);
						exportBtn.setText(LANG_TEXT[langIndex][12]);
						exportAllBtn.setText(LANG_TEXT[langIndex][13]);
						langMenu.setText(LANG_TEXT[langIndex][15]);
						for (int i=0; i<langs.length; i++) langs[i].setText(LANG_NAMES[langIndex][i]);
						//change dropdown menu text
						int lastSelIndex = dropdown.getSelectedIndex();
						System.arraycopy(LANG_TEXT[langIndex], 16, Main.SECT_NAMES, 0, 9);
						dropdown.setModel(new DefaultComboBoxModel<String>(Main.SECT_NAMES));
						dropdown.setSelectedIndex(lastSelIndex);	
					} catch (IOException ex) {
						errorBeep();
						JOptionPane.showMessageDialog(frame, ex.getClass().getSimpleName()+": "+ex.getLocalizedMessage(), LANG_TEXT[langIndex][8], 0);
					}
				}
			});
		}
		//add components
		btnBox.add(exportBtn);
		btnBox.add(Box.createHorizontalStrut(20));
		btnBox.add(exportAllBtn);
		fileMenu.add(open);
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(langMenu);
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
			errorBeep();
			JOptionPane.showMessageDialog(null, e.getClass().getSimpleName()+": "+e.getLocalizedMessage(), LANG_TEXT[langIndex][8], 0);
		}
	}
}