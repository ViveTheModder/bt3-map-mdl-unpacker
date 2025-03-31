package cmd;
//Budokai Tenkaichi 3 Map Model Unpacker by ViveTheModder
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import gui.App;

public class Main 
{
	public static boolean isGuiDisabled=true;
	private static final int[] MDL_POSITIONS = {28,36,44,52,60,64,72,80,84};
	public static final String[] SECT_NAMES = 
	{"Destructible Objects' Coordinates","Map Model Data","Map Model Part Data",
	"Rendering Indices","Map Texture Container (DBT)","Rendering Distances",
	"Collision Data","Destructible Objects Data","Barrier Parameters"};
	static final String[] SECT_FILENAMES = 
	{"destruct_objs_xyz.dat","model.dat","model_parts.dat",
	"rendering_idx.dat","map_txs.dbt","rendering_dst.dat",
	"collision.dat","destruct_obj.dat","barrier_param.dat"};
	private static byte[] getMdlSection(byte[] mdl, int sectIndex)
	{
		byte[] offset = new byte[4];
		int[] sectPos = new int[MDL_POSITIONS.length+1];
		for (int i=0; i<MDL_POSITIONS.length; i++)
		{
			System.arraycopy(mdl, MDL_POSITIONS[i], offset, 0, 4);
			sectPos[i] = 4*LittleEndian.getIntFromByteArray(offset);
		}
		sectPos[MDL_POSITIONS.length] = mdl.length;
		Arrays.sort(sectPos);
		int sectSize = sectPos[sectIndex+1]-sectPos[sectIndex];
		byte[] section = new byte[sectSize];
		System.arraycopy(mdl, sectPos[sectIndex], section, 0, sectSize);
		return section;
	}
	private static byte[] getMdl(RandomAccessFile map) throws IOException
	{
		map.seek(4);
		int start = LittleEndian.getInt(map.readInt());
		int end = LittleEndian.getInt(map.readInt());
		int mdlSize = end-start;
		byte[] mdl = new byte[mdlSize];
		map.seek(start);
		map.read(mdl);
		return mdl;
	}
	private static void setLangIndexFromLocale()
	{
		Locale loc = Locale.getDefault(Locale.Category.FORMAT);
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
			String[] fileNameArray = fileName.split("-");
			int id = Integer.parseInt(fileNameArray[1]);
			String abbr = fileNameArray[2].substring(0,fileNameArray[2].length()-4);
			if (abbr.equals(loc.getLanguage())) App.langIndex=id;
		}
	}
	public static RandomAccessFile getMap(File src, String fileExt) throws IOException
	{
		RandomAccessFile map = new RandomAccessFile(src,"r");
		if (fileExt.equals("pak") || fileExt.equals("unk"))
		{
			int numSections = LittleEndian.getInt(map.readInt());
			map.seek(104);
			int actualFileSize = (int)map.length();
			int expectedFileSize = LittleEndian.getInt(map.readInt());
			if (actualFileSize!=expectedFileSize || numSections!=25) return null;
			else return map;
		}
		else if (fileExt.equals("mdl")) return map;
		return null;
	}
	public static void writeSectionToFile(File src, RandomAccessFile map, int sectIndex) throws IOException
	{
		String fileName = src.getName();
		String fileNameWithoutExt = fileName.substring(0, fileName.length()-4);
		String srcFolderPath = src.getCanonicalPath().replace(fileName, "");
		File outFolder = new File(srcFolderPath+fileNameWithoutExt);
		if (map!=null) outFolder.mkdir();
		int numSections=1;
		if (sectIndex<0) numSections = SECT_NAMES.length;
		for (int i=0; i<numSections; i++)
		{
			if (numSections!=1) sectIndex=i;
			File out = new File(outFolder.getCanonicalPath()+File.separator+sectIndex+"_"+SECT_FILENAMES[sectIndex]);
			byte[] mdl = getMdl(map);
			byte[] section = getMdlSection(mdl,sectIndex);
			RandomAccessFile raf = new RandomAccessFile(out,"rw");
			//in case a map is changed and then unpacked, the section contents are erased first then overwritten
			if (out.exists()) raf.setLength(0); 
			if (isGuiDisabled) 
				System.out.println(App.LANG_TEXT[App.langIndex][25].replace("[section]", SECT_NAMES[sectIndex]).replace("[filename]", fileName));
			raf.write(section);
			raf.close();
		}
		//check if the given map is made by Chuchoman/Kinnikuchu, then fix the file names (because the sections are ordered differently)
		File[] sectFiles = outFolder.listFiles();
		if (sectFiles.length==9)
		{
			if (sectFiles[8].length()>64)
			{
				Files.move(sectFiles[8].toPath(), sectFiles[8].toPath().resolveSibling(8+"_"+SECT_FILENAMES[4]));
				Files.move(sectFiles[7].toPath(), sectFiles[7].toPath().resolveSibling(7+"_"+SECT_FILENAMES[8]));
				Files.move(sectFiles[4].toPath(), sectFiles[7].toPath().resolveSibling(4+"_"+SECT_FILENAMES[7]));
			}
		}
	}
	public static void main(String[] args) throws IOException
	{
		setLangIndexFromLocale();
		if (App.langIndex!=0) 
		{
			App.setLangText();
			System.arraycopy(App.LANG_TEXT[App.langIndex], 16, Main.SECT_NAMES, 0, 9);
		}
		if (args.length>0)
		{
			if (args[0].equals("-c"))
			{	
				int sectNum=-1;
				File src=null;
				RandomAccessFile map=null;
				Scanner sc = new Scanner(System.in);
				while (src==null)
				{
					System.out.println(App.LANG_TEXT[App.langIndex][26]);
					String path = sc.nextLine();
					File temp = new File(path);
					String fileName = temp.getName().toLowerCase();
					String fileExt = fileName.replaceAll("^.*\\.(.*)$", "$1");
					if (fileExt.equals("pak") || fileExt.equals("mdl") || fileExt.equals("unk"))
					{
						map = getMap(temp,fileExt);
						if (map!=null) src=temp;
					}
				}
				while (sectNum<0)
				{
					System.out.println(App.LANG_TEXT[App.langIndex][27]);
					System.out.println("0. "+App.LANG_TEXT[App.langIndex][13]);
					for (int i=0; i<SECT_NAMES.length; i++) System.out.println((i+1)+". "+SECT_NAMES[i]);
					String input = sc.nextLine();
					if (input.matches("\\d+")) sectNum = Integer.parseInt(input);
				}
				sc.close();
				sectNum--;
				long start = System.currentTimeMillis();
				writeSectionToFile(src,map,sectNum);
				long finish = System.currentTimeMillis();
				double time = (finish-start)/1000.0;
				System.out.println(App.LANG_TEXT[App.langIndex][28].replace("[time]", ""+time+""));
			}
		}
		else 
		{
			isGuiDisabled=false;
			App.main(args);
		}
	}
}