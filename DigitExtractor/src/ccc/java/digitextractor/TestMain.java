package ccc.java.digitextractor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.java.digitextractor.data.BackGroundShade;
import ccc.java.restclient.RestClient;

public class TestMain
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// System.loadLibrary("native_sample");

		// /String debugZiffern = "0185774";
		// String debugZiffern = "2065189";
		// String debugZiffern = "1658443";
		String debugZiffern = "1270860";
		ImageStatics.Imagename = "CIMG1904.jpg";
		RestClient client = null;

		try
		{
			client = new RestClient(new URL("http://aldebaran:89/MobileGaugeReading"));
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Map<String, Object> restrmap = new HashMap<String, Object>();
		// restrmap.put("id", 0);
		// restrmap.put("methodname", "GetGaugeDeviceDigitList");
		// ParameterMap id = new ParameterMap(restrmap);
		GaugeDeviceDigitList standardDigits = null;
		try
		{
			String json = ImageStatics.ReadTextFile("C:\\Users\\MarkusF.CCCSW\\Desktop\\digtpatterns.txt");
			standardDigits = client.getMapper().readValue(json, GaugeDeviceDigitList.class);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Map<Integer, List<Mat>> patterns =
		// ImageStatics.GetDigitImages("C:\\Users\\MarkusF\\Pictures");

		List<String> files = new ArrayList<String>();

		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/20140919_135616.jpg");
		File[] allFiles = ImageStatics.GetFilesOfFolder("C:\\Users\\MarkusF.CCCSW\\Pictures\\Zahlerbilder\\debug2");
		for (File f : allFiles)
		{
			files.add(f.getAbsolutePath());
		}
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230656.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230813.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230807.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230800.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230751.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230744.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230737.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230730.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230722.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140903_230713.jpg");
		//
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124953.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124943.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124932.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124923.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124912.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124906.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124855.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124842.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140906_124831.jpg");

		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142934.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142928.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142922.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142916.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142910.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142902.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142855.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142850.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_142844.jpg");

		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_154037.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_154032.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_154024.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_154004.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153935.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153927.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153921.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_154011.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153958.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153951.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153945.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153940.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153904.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153858.jpg");
		// files.add("C:/Users/MarkusF/Pictures/Zahlerbilder/IMG_20140908_153852.jpg");
		DigitReadingProcessor proc = new DigitReadingProcessor(ImageStatics.ConvertGaugeDeviceDigitListToBinaryPatternContainer(standardDigits), 256);
		proc.initializeReadingSession(BackGroundShade.Bright, 7, 3);
		Date d = new Date();

		for (int j = 0; j < files.size(); j = j + 5)
		{
			for (int i = j; i < Math.min(j + 5, files.size()); i++)
			{
				if (ImageStatics.DEV_MODE)
				{
					try
					{
						Process p = Runtime.getRuntime().exec("cmd /c C:/Users/MarkusF.CCCSW/workspace/DigitExtractor/del.bat");
						p.waitFor();
					}
					catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				proc.addReadingImage(files.get(i), true);
				proc.processLastEntry();
				// System.out.println(proc.getCurrent().getExtractedBestValues());
				System.out.println(proc.getCurrent().getPreciseChars());
				// System.out.println(proc.getPrecisions().get(proc.getPrecisions().size()-1).toString());
				// try {
				// Process p =
				// Runtime.getRuntime().exec("cmd /c C:/Users/Chile/workspace/DigitExtractor/del.bat");
				// p.waitFor();
				// } catch (IOException e1) {
				// // TODO Auto-generated catch block
				// e1.printStackTrace();
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
			proc.alignResults();
			proc.getAligner().printScoreboards();
			System.out.println("\nfinal result:");
			System.out.println(proc.getAligner().getCurrentResult());
		}
		System.out.println(new Date().getTime() - d.getTime());
		proc.getCameraOrientation();
		// try {
		// images = imProc.ExtractDigits();
		// } catch (FrameNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println(new Date().getTime() - d.getTime());
		// System.out.println(imProc.getExtractedavgvalues());
		// System.out.println(imProc.getExtractedBestValues());
		// System.out.println(imProc.getPreciseChars());
		// System.out.println(imProc.getResult());
		//
		// imProc.release();

		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		GaugeDeviceDigitList digitList = new GaugeDeviceDigitList();

		// for(int i = 0; i< images.size(); i++)
		// {
		// if (images.get(i) != null) {
		// GaugeDeviceDigit digit = new GaugeDeviceDigit();
		// digit.setBinary(images.get(i));
		// if (debugZiffern.length() > i) {
		// digit.setDigit(Integer.parseInt(debugZiffern.substring(i, i + 1)));
		// digit.setGaugeDeviceId(198);
		// digitList.add(digit);
		// }
		// }
		// }
		// try {
		// client.PostMultipleObjectsToServer(digitList, null);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

}
