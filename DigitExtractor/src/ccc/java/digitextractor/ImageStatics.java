package ccc.java.digitextractor;

import static org.bytedeco.javacpp.helper.opencv_imgproc.cvFindContours;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.LUT;
import static org.bytedeco.javacpp.opencv_core.inRange;
import static org.bytedeco.javacpp.opencv_core.line;
import static org.bytedeco.javacpp.opencv_core.mean;
import static org.bytedeco.javacpp.opencv_core.rectangle;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_COLOR;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.BORDER_CONSTANT;
import static org.bytedeco.javacpp.opencv_imgproc.CV_ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_LIST;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static org.bytedeco.javacpp.opencv_imgproc.adaptiveThreshold;
import static org.bytedeco.javacpp.opencv_imgproc.boundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.copyMakeBorder;
import static org.bytedeco.javacpp.opencv_imgproc.cvBoundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.threshold;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;

import ccc.android.meterdata.listtypes.GaugeDeviceDigitList;
import ccc.android.meterdata.types.GaugeDeviceDigit;
import ccc.java.digitextractor.data.BackGroundShade;
import ccc.java.digitextractor.data.RectExt;
import ccc.java.digitextractor.exceptions.FrameNotFoundException;
import ccc.java.digitextractor.exceptions.MatrixMissmatchException;
import ccc.java.kmeans.KPoint;
import ccc.java.kmeans.KPointCollection;
import ccc.java.simpleocr.BinaryPattern;
import ccc.java.simpleocr.BinaryPatternContainer;

public class ImageStatics
{
	// DEBUG-STUFF
	public static final String ROOT = "C:\\Users\\MarkusF.CCCSW\\Pictures\\DigitExtracorDebug";
	private static int staticCount = 0;
	public static final boolean DEV_MODE = false;

	public static void putOutDigitsAsFiles(Mat bitImage, List<Rect> boxes, String outputPath)
	{

		if (ImageStatics.DEV_MODE)
		{
			if (boxes == null)
				return;
			Path path = Paths.get(outputPath);
			if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS))
			{
				(new File(outputPath)).mkdirs();
			}
			for (int i = 0; i < boxes.size(); i++)
			{
				Rect boxi = boxes.get(i);
				if (boxi != null && boxi.width() >= 0 && boxi.height() >= 0 && boxi.x() < bitImage.size().width()
						&& boxi.y() < bitImage.size().height())
				{
					Mat zw = new Mat(boxi.width(), boxi.height(), bitImage.type());
					zw = bitImage.apply(boxi);
					imwrite(outputPath + "\\digit" + staticCount + ".jpg", zw);
				}
				staticCount++;
			}
		}
	}

	public static void putOutDigitsAsFiles(List<Mat> imageList, String outputPath, String stump)
	{
		if (ImageStatics.DEV_MODE)
		{
			if (imageList == null)
				return;
			Path path = Paths.get(outputPath);
			if (stump == null)
				stump = "\\digit%d.jpg";
			else
				stump = "\\" + stump + "-" + ImageStatics.Imagename + "-%d.jpg";
			if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS))
			{
				(new File(outputPath)).mkdirs();
			}
			for (int i = 0; i < imageList.size(); i++)
			{
				if (imageList.get(i) != null)
				{
					imwrite(path + "\\" + String.format(stump, i), imageList.get(i));
				}
			}
		}
	}

	// public static Map<Integer, List<Mat>> GetDigitImages(String path)
	// {
	// Map<Integer, List<Mat>> images = new HashMap<Integer, List<Mat>>();
	// File[] allFiles = GetFilesOfFolder(path);
	// for(File f : allFiles)
	// {
	// String extention =
	// FilenameUtils.getExtension(f.getAbsolutePath()).toLowerCase();
	// if(extention.equals("jpg"))
	// {
	// Mat zw = GetDigitIplFromFile(f.getAbsolutePath());
	// int id = zw.ID();
	// if(images.get(id) == null)
	// images.put(id, new ArrayList<Mat>());
	// if(zw.ID() >=0 && zw.ID() < 10)
	// images.get(id).add(zw);
	// }
	// }
	// return images;
	// }

	// //DEBUG-STUFF-END////

	// accuracy evaluation
	public static final double WORST_ACCURACY_VALUE = 1.6;

	// cluster evaluation
	public static final double CLUSTER_X_VARIATION_PERC = 0.05;
	public static final double CLUSTER_HEIGHT_VARIATION_PERC = 0.2;
	public static final double CLUSTER_INTERNAL_HEIGHT_VARIATION_PERC = 0.1;
	public static final double CLUSTER_DISTANCE_VARIATION_PERC = 0.15;
	public static final double CLUSTER_DISTANCE_DEVIATION_PERC = 0.33;

	public static final int BRIGHTNESS_STEP_DISTANCE = 16; // make binary image
															// of display
															// between 0 and 127
															// by this step
															// distance

	public static final int DIGIT_SIGNAL_VAL_RGB_H_MIN = 285;
	public static final int DIGIT_SIGNAL_VAL_RGB_H_MAX = 315;
	public static final int DIGIT_SIGNAL_VAL_RGB_S_MIN = 75;
	public static final int DIGIT_SIGNAL_VAL_RGB_S_MAX = 100;
	public static final int DIGIT_SIGNAL_VAL_RGB_V_MIN = 75;
	public static final int DIGIT_SIGNAL_VAL_RGB_V_MAX = 100;

	public static final int LOW_CONTRAST_COLOR1_RGB_H_MIN = 0;
	public static final int LOW_CONTRAST_COLOR1_RGB_H_MAX = 20;
	public static final int LOW_CONTRAST_COLOR1_RGB_S_MIN = 20;
	public static final int LOW_CONTRAST_COLOR1_RGB_S_MAX = 100;
	public static final int LOW_CONTRAST_COLOR1_RGB_V_MIN = 40;
	public static final int LOW_CONTRAST_COLOR1_RGB_V_MAX = 100;

	public static final int LOW_CONTRAST_COLOR2_RGB_H_MIN = 340;
	public static final int LOW_CONTRAST_COLOR2_RGB_H_MAX = 360;
	public static final int LOW_CONTRAST_COLOR2_RGB_S_MIN = 20;
	public static final int LOW_CONTRAST_COLOR2_RGB_S_MAX = 100;
	public static final int LOW_CONTRAST_COLOR2_RGB_V_MIN = 40;
	public static final int LOW_CONTRAST_COLOR2_RGB_V_MAX = 100;

	public static final int BRIGHTNES_MIN_LEVEL = 30;
	public static final int BRIGHTNES_MAX_LEVEL = 0;
	public static final int CONTRAST_MIN_LEVEL = 60;
	public static final int CONTRAST_MAX_LEVEL = 128;

	public static final int ADAPTIVE_THRESHOLD_BLOCK_SIZE = 11;
	public static final int ADAPTIVE_THRESHOLD_WEIGHT_CONST = 5;

	public static final int DIGIT_STORE_SIZE_WIDTH = 70;
	public static final int DIGIT_STORE_SIZE_HEIGHT = 90;

	public static final float DIGIT_MIN_HEIGHT = 1.2f;
	public static final float DIGIT_MAX_HEIGHT = 6f;
	public static final float DIGIT_MIN_RATIO = 1.3f;
	public static final float DIGIT_MAX_RATIO = 8f;
	public static final float DIGIT_MIN_HEIGHT_PIXELS = 33;

	public static final double BACKGROUND_SHADE_THRESHOLD = 128d;
	public static final int BRIGHTNESS_DARK_BACKGROUND = 150;
	public static final int BRIGHTNESS_BRIGHT_BACKGROUND = 140;

	public static final int BINARY_IMAGE_THRESHOLD = 150;

	public static String Imagename = "";

	public static File[] GetFilesOfFolder(String path)
	{
		Path p = Paths.get(path);
		if (Files.exists(p))
		{
			File folder = new File(path);
			if (folder.isDirectory())
			{
				return folder.listFiles();
			}
		}
		return new File[0];
	}

	public static String ReadTextFile(String path) throws IOException
	{
		String everything = null;
		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null)
			{
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			everything = sb.toString();
		}
		return everything;
	}

	public static Mat GetDigitIplFromFile(String filePath)
	{
		Path path = Paths.get(filePath);
		if (Files.exists(path))
		{
			Mat zw = imread(filePath, CV_LOAD_IMAGE_COLOR);
			// String FileName = file.getName();
			// try {
			// zw.ID(Integer.parseInt(file.getName().substring(0,1)));
			// } catch (Exception e) {
			// zw.ID(-1);
			// }
			return zw;
		}
		return null;
	}

	public static List<byte[]> ConvertImagesToByteArrays(List<Mat> orderedDigits)
	{
		List<byte[]> imageByteList = new ArrayList<byte[]>();

		for (int i = 0; i < orderedDigits.size(); i++)
		{
			if (orderedDigits.get(i) != null)
			{

				ByteBuffer zw = orderedDigits.get(i).getByteBuffer();
				byte[] buffer = new byte[(int) (orderedDigits.get(i).total() * orderedDigits.get(i).elemSize())];
				zw.get(buffer);
				imageByteList.add(buffer);
			}
			else
				imageByteList.add(null);
		}
		return imageByteList;
	}

	public static Mat ConvertByteArrayToImage(int width, int height, int cvType, byte[] data) throws MatrixMissmatchException
	{
		Mat mat = new Mat(height, width, cvType);
		if (mat.getByteBuffer().capacity() != data.length)
			throw new MatrixMissmatchException("type does not match data");
		try
		{
			mat.getByteBuffer().put(data);
		}
		catch (BufferOverflowException boe)
		{
			throw new MatrixMissmatchException("type does not match data");
		}
		return mat;
	}

	// public static Mat ConvertByteArrayToMat(byte[] data, int width, int
	// height)
	// {
	// try {
	// Mat bgra = Mat.create(width, height, IPL_DEPTH_8U, 4);
	// cvCvtColor(yuv,bgra,CV_YUV2BGR_NV21);
	// return bgra;
	// } catch (Exception e) {
	// if(!e.getMessage().contains("java.awt")) //not!!
	// throw e;
	// }
	// return null;
	// }

	// public static int[] decodeYUV420SP(byte[] yuv420sp, Rect rect)
	// {
	// if((rect.x()+rect.width())*(rect.y()+rect.height())*1.5 >
	// yuv420sp.length)
	// throw new ArrayIndexOutOfBoundsException();
	// int frameSize = rect.width() * rect.height();
	// int[] rgb = new int[frameSize];
	// for (int j = rect.y(), yp = (rect.y()*rect.width()+rect.x()); j <
	// rect.height(); j++) {
	// int uvp = frameSize + (j >> 1) * rect.width(), u = 0, v = 0;
	// for (int i = rect.x(); i < rect.width(); i++, yp++) {
	// int y = (0xff & ((int) yuv420sp[yp])) - 16;
	// if (y < 0) y = 0;
	// if ((i & 1) == 0) {
	// v = (0xff & yuv420sp[uvp++]) - 128;
	// u = (0xff & yuv420sp[uvp++]) - 128;
	// }
	// int y1192 = 1192 * y;
	// int r = (y1192 + 1634 * v);
	// int g = (y1192 - 833 * v - 400 * u);
	// int b = (y1192 + 2066 * u);
	// if (r < 0) r = 0; else if (r > 262143) r = 262143;
	// if (g < 0) g = 0; else if (g > 262143) g = 262143;
	// if (b < 0) b = 0; else if (b > 262143) b = 262143;
	// rgb[yp] = 0xff000000 | ((b << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((r
	// >> 10) & 0xff);
	// }
	// }
	// return rgb;
	// }

	public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height, Rect rect)
	{
		final int frameSize = width * height;
		int rgb[] = new int[rect.width() * rect.height()];
		for (int j = 0, yp = 0, ic = 0; j < height; j++)
		{
			if (j <= rect.y())
			{
				yp += width;
				continue;
			}
			if (j > rect.y() + rect.height())
				break;
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++)
			{
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0)
				{
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}
				if (i <= rect.x() || i > rect.x() + rect.width())
					continue;
				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				int rx = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
				rgb[ic] = rx;
				ic++;
			}
		}
		return rgb;
	}

	// public static List<Image> ConvertImagesToOcrImage(List<byte[]>
	// orderedDigits)
	// {
	// List<Image> output = new ArrayList<Image>();
	//
	// for(int i=0; i < orderedDigits.size();i++)
	// {
	// if(orderedDigits.get(i) != null)
	// {
	// InputStream in = new ByteArrayInputStream(orderedDigits.get(i));
	// BufferedImage zw;
	// try {
	// zw = ImageIO.read(in);
	// Image img = zw.getScaledInstance(ImageStatics.DIGIT_STORE_SIZE_WIDTH,
	// ImageStatics.DIGIT_STORE_SIZE_HEIGHT, 0);
	// output.add(img);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// output.add(null);
	// }
	//
	// }
	// else
	// output.add(null);
	// }
	// return output;
	// }

	public static Map<Character, List<byte[]>> ConvertGaugeDeviceDigitListToByteArrays(GaugeDeviceDigitList list)
	{
		Map<Character, List<byte[]>> map = new HashMap<Character, List<byte[]>>();
		for (GaugeDeviceDigit digit : list.getDigitList())
		{
			if (map.get(String.valueOf(digit.getDigit()).charAt(0)) == null)
				map.put(String.valueOf(digit.getDigit()).charAt(0), new ArrayList<byte[]>());
			map.get(String.valueOf(digit.getDigit()).charAt(0)).add(digit.getBinary());
		}
		return map;
	}

	public static BinaryPatternContainer ConvertGaugeDeviceDigitListToBinaryPatternContainer(GaugeDeviceDigitList list)
	{
		BinaryPatternContainer cont = new BinaryPatternContainer();
		for (GaugeDeviceDigit digit : list.getDigitList())
		{
			Character chr = String.valueOf(digit.getDigit()).charAt(0);
			List<BinaryPattern> patterns = cont.get(chr);
			if (patterns == null)
				patterns = new ArrayList<BinaryPattern>();
			try
			{
				patterns.add(new BinaryPattern(chr, ImageStatics.DIGIT_STORE_SIZE_WIDTH, ImageStatics.DIGIT_STORE_SIZE_HEIGHT, digit.getBinary()));
			}
			catch (MatrixMissmatchException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cont.put(chr, patterns);
		}
		return cont;
	}

	public static List<CvRect> GetContoursOfImage(Mat input)
	{
		CvSeq contours = new CvSeq();

		int check = cvFindContours(input.asCvMat(), CvMemStorage.create(), contours, Loader.sizeof(CvContour.class), CV_RETR_LIST,
				CV_CHAIN_APPROX_SIMPLE);

		List<CvRect> rects = new ArrayList<CvRect>();
		CvSeq zw = new CvSeq();
		if (contours != null)
		{
			for (zw = contours; zw != null && check != 0; zw = zw.h_next())
			{
				rects.add(cvBoundingRect(zw, 0));
			}

		}
		return rects;
	}

	public static BackGroundShade GetBackgroundShade(Mat image)
	{
		Scalar scal = mean(image, null);

		if (scal.get(0) < BACKGROUND_SHADE_THRESHOLD)
			return BackGroundShade.Dark;
		else
			return BackGroundShade.Bright;
	}

	public static Mat CropImage(Mat orgImg, Rect frame)
	{
		if (frame.width() <= orgImg.size().width() && frame.height() <= orgImg.size().height())
		{
			orgImg = orgImg.apply(frame);
			return orgImg;
		}
		return null;
	}

	public static Mat GetBinaryImage(Mat image, int thresholdValue, BackGroundShade shade)
	{
		if (shade == BackGroundShade.Dark)
			threshold(image, image, 255 - thresholdValue, 255, CV_THRESH_BINARY_INV);
		else if (shade == BackGroundShade.Bright)
			threshold(image, image, thresholdValue, 255, CV_THRESH_BINARY);
		byte[] data = new byte[image.getByteBuffer().capacity()];
		image.getByteBuffer().get(data);
		return image;
	}

	public static Mat AddFrameToImage(Mat image, int frameWidth, BackGroundShade shade)
	{
		Mat framedImage = new Mat(image.size().width() + frameWidth * 2, image.size().height() + frameWidth * 2, image.type());
		if (shade == BackGroundShade.Bright)
			copyMakeBorder(image.clone(), framedImage, frameWidth, frameWidth, frameWidth, frameWidth, BORDER_CONSTANT, Scalar.all(255));
		else if (shade == BackGroundShade.Dark)
			copyMakeBorder(image.clone(), framedImage, frameWidth, frameWidth, frameWidth, frameWidth, BORDER_CONSTANT, Scalar.all(0));

		return framedImage;
	}

	public static Mat DrawRectanglesIntoImage(Mat image, List<Rect> rectangles, Scalar color, int thickness)
	{
		for (int i = 0; i < rectangles.size(); i++)
		{
			Rect rect = rectangles.get(i);
			rectangle(image, new Point(rect.x(), rect.y()), new Point(rect.x() + rect.width(), rect.y() + rect.height()), color, thickness, 8, 0);
		}
		return image;
	}

	// public static List<Vector<Point>> GetEdges( Mat image)
	// {
	// Mat work = new Mat(image.size(), image.type());
	// Canny(image, work, 140, 70, 3, false);
	// HoughLines( work, work, 1, Math.PI / 180, 40, 1, 1);
	// List<Vector<CvPoint>> edges = new ArrayList<Vector<CvPoint>>();
	//
	// for (int i = 0; i <= lines.total(); i++)
	// {
	// Pointer line = cvGetSeqElem(lines, i);
	// CvPoint pt1 = new CvPoint(line).position(0);
	// CvPoint pt2 = new CvPoint(line).position(1);
	// Vector<CvPoint> vec = new Vector<CvPoint>();
	// vec.add(pt1);
	// vec.add(pt2);
	// edges.add(vec);
	// }
	// return edges;
	// }

	public static Mat DrawLines(Mat image, List<Vector<Point>> lines)
	{
		for (int i = 0; i < lines.size(); i++)
		{
			Point pt1 = lines.get(i).get(0);
			Point pt2 = lines.get(i).get(1);
			// TODO Farbe
			line(image, pt1, pt2, new Scalar(180, 100, 100, 80), 3, 8, 0); // draw
																			// the
																			// segment
																			// on
																			// the
																			// image
		}
		return image;
	}

	public static Mat ChangeBrightnessContrast(Mat image, int brightness, int contrast)
	{ /*
	 * The algorithm is by Werner D. Streidt (http://visca
	 * .com/ffactory/archives/5-99/msg00021.html) (note: uses values between -1
	 * and 1)
	 */
		Mat retMat = new Mat(image.size(), image.type());
		BytePointer myPtr = new BytePointer(256 * 3);
		Mat briConLutMatrix = new Mat(1, 256, CV_8UC1, myPtr);
		byte[] buff = new byte[(int) (briConLutMatrix.total() * briConLutMatrix.channels())];
		briConLutMatrix.getByteBuffer().get(buff);
		double delta, a, b = 0;

		if (contrast > 0)
		{
			delta = 127. * contrast / 128;
			a = 255. / (255. - delta * 2);
			b = a * (brightness - delta);
		}
		else
		{
			delta = -128. * contrast / 128;
			a = (256. - delta * 2) / 255.;
			b = a * brightness + delta;
		}

		for (int i = 0; i < 256; i++)
		{
			int v = (int) Math.round(a * i + b);
			if (v < 0)
				v = 0;
			if (v > 255)
				v = 255;
			// TODO check this
			buff[i] = (byte) v;
		}
		briConLutMatrix.getByteBuffer().put(buff);
		LUT(image, briConLutMatrix, retMat);
		// briConLutMatrix.release();
		return retMat;
	}

	public static Mat SetColorByMask(Mat original, Mat mask, Scalar color) throws Exception
	{
		byte[] buff = new byte[original.getByteBuffer().capacity()];
		original.getByteBuffer().get(buff);
		byte[] maskBuf = new byte[mask.getByteBuffer().capacity()];
		mask.getByteBuffer().get(maskBuf);
		if (original.size().width() == original.size().width() && mask.size().height() == mask.size().height())
		{
			for (int i = 0; i < maskBuf.length; i++)
			{
				if (maskBuf[i] != 0)
				{
					buff[i * 3] = (byte) color.get(1);
					buff[i * 3 + 1] = (byte) color.get(2);
					buff[i * 3 + 2] = (byte) color.get(3);
				}
			}
			original.getByteBuffer().put(buff);
			return new Mat(original);
		}
		throw new Exception("mask has not the same size as original");
	}

	public static List<Rect> DeleteOverLappingBoundingBoxes(List<Rect> input)
	{
		List<Rect> boxes = new ArrayList<Rect>();
		boxes.addAll(input);
		for (int i = boxes.size() - 1; i >= 0; i--)
		{
			for (int j = boxes.size() - 1; j >= 0; j--)
			{
				if (!boxes.get(i).equals(boxes.get(j))) // not!
				{
					Rect boxi = boxes.get(i);
					Rect boxj = boxes.get(j);
					int areai = boxi.height() * boxi.width();
					int areaj = boxj.height() * boxj.width();
					// check top left and bottom right corner
					if (boxi.x() >= boxj.x() && boxi.x() <= boxj.x() + boxj.width() && boxi.y() >= boxj.y() && boxi.y() <= boxj.y() + boxj.height())
					{
						if (areai <= areaj)
							boxes.remove(i);
						else
							boxes.remove(j);
						break;
					}
					// check top right and bottom left corner
					if (boxi.x() + boxi.width() >= boxj.x() && boxi.x() + boxi.width() <= boxj.x() + boxj.width()
							&& boxi.y() + boxi.height() >= boxj.y() && boxi.y() + boxi.height() <= boxj.y() + boxj.height())
					{
						if (areai <= areaj)
							boxes.remove(i);
						else
							boxes.remove(j);
						break;
					}
				}
			}
		}
		return boxes;
	}

	public static Mat NormalizeBrightness(Mat image)
	{
		int brightness = GetBrightness(image);
		image = ChangeBrightnessContrast(image, brightness, 128);
		return image;
	}

	public static int GetBrightness(Mat image)
	{
		Scalar scal = mean(image, null);
		return (int) scal.get(0);
	}

	public static Scalar getOpenCvHSV(int h, int s, int v)
	{
		return new Scalar((double) h / 2, (double) s * 2.55, (double) v * 2.55, 0d);
	}

	public static Mat getHsvColorMask(Mat original, Scalar minColor, Scalar maxColor)
	{
		Mat imgThreshold = new Mat(new Size(original.size().width() + 2, original.size().height() + 2), original.type());
		// TODO check if needed or right
		inRange(original, new Mat(minColor), new Mat(maxColor), imgThreshold);
		return imgThreshold;
	}

	public static List<RectExt> extractExtendedCvRect(List<Rect> list)
	{
		List<RectExt> output = new ArrayList<RectExt>();
		for (Rect rect : list)
		{
			if (rect instanceof RectExt)
				output.add((RectExt) rect);
		}
		return output;
	}

	public static double getListAverage(List<Integer> list)
	{
		int sum = 0;
		for (int el : list)
			sum += el;
		return (double) sum / (double) list.size();
	}

	public static double getGrayScaleListAverage(List<Mat> grayscales)
	{
		double averagePixelDens = 0;

		for (Mat img : grayscales)
		{
			averagePixelDens += mean(img, null).get(0);
		}
		return averagePixelDens / grayscales.size();
	}

	public static List<Rect> getClusterFromKPointCollection(List<RectExt> inputCluster, KPointCollection newCluster)
	{
		List<Rect> reducedCluster = new ArrayList<Rect>();
		for (KPoint point : newCluster)
		{
			reducedCluster.add(inputCluster.get(point.getId()));
		}
		return reducedCluster;
	}

	public static List<List<Mat>> getImagesFromFrameList(List<List<Rect>> frames, Mat bitImage)
	{
		List<List<Mat>> output = new ArrayList<List<Mat>>();
		for (List<? extends Rect> inner : frames)
		{
			if (inner != null)
				output.add(getImagesFromFrames(inner, bitImage));
			else
				output.add(null);
		}

		return output;
	}

	public static List<Mat> getImagesFromFrames(List<? extends Rect> frames, Mat bitImage)
	{

		List<Mat> orderedDigits = new ArrayList<Mat>();
		for (int i = 0; i < frames.size(); i++)
		{
			Rect frame = frames.get(i);

			if (frame != null && frame.x() + frame.width() < bitImage.size().width() && frame.y() + frame.height() < bitImage.size().height())
			{

				Mat zw = bitImage.clone();
				if (zw != null)
				{
					if (frame instanceof RectExt)
						zw = ImageStatics.ChangeBrightnessContrast(zw, ((RectExt) frame).getBrightness(), 128);
					zw = ImageStatics.CropImage(zw, frame);
					orderedDigits.add(zw);

				}
			}
			else
				orderedDigits.add(null);
		}
		return orderedDigits;
	}

	public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValue(Map<K, V> map, boolean ascending)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		if (ascending)
		{
			Collections.sort(list, new Comparator<Map.Entry<K, V>>()
			{
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});
		}
		else
		{
			Collections.sort(list, new Comparator<Map.Entry<K, V>>()
			{
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				{
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});
		}

		LinkedHashMap<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static List<CvRect> getBoundingBoxesOfContoures(Mat bitImage, BackGroundShade shade)
	{
		return getBoundingBoxesOfContoures(bitImage, shade, ImageStatics.ADAPTIVE_THRESHOLD_BLOCK_SIZE, ImageStatics.ADAPTIVE_THRESHOLD_WEIGHT_CONST);
	}

	public static List<CvRect> getBoundingBoxesOfContoures(Mat bitImage, BackGroundShade shade, int blockSize, int weightConstant)
	{
		Mat contourImage = bitImage;
		List<CvRect> boundingBoxes = new ArrayList<CvRect>();
		if (shade == BackGroundShade.Dark || shade == BackGroundShade.Undef)
		{
			adaptiveThreshold(bitImage, contourImage, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY, blockSize, weightConstant);
			boundingBoxes.addAll(ImageStatics.GetContoursOfImage(contourImage));
		}
		if (shade == BackGroundShade.Bright || shade == BackGroundShade.Undef)
		{
			adaptiveThreshold(bitImage, contourImage, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY_INV, blockSize, weightConstant);
			boundingBoxes.addAll(ImageStatics.GetContoursOfImage(contourImage));
		}
		return boundingBoxes;
	}

	public static List<Rect> extractRectanglesFromMatVector(MatVector contours) throws FrameNotFoundException
	{
		List<Rect> outout = new ArrayList<Rect>();
		for (int i = 0; i < contours.size(); i++)
		{
			try
			{
				Rect zw = boundingRect(contours.get(i));
				if (zw != null)
					outout.add(zw);
			}
			catch (RuntimeException e)
			{
				throw new FrameNotFoundException();
			}
		}
		return outout;
	}

	public static Mat ByteArrayToMatrix(byte[] bytes, int width, int height)
	{
		Mat zw = new Mat(height, width, org.bytedeco.javacpp.opencv_core.CV_8UC1);
		zw.getByteBuffer().put(bytes);
		return zw;
	}

	public static int[] convertByteArrayToIntArray(byte[] bytes)
	{
		int[] ints = new int[bytes.length];
		for (int index = 0; index < bytes.length; index++)
		{
			if (bytes[index] < 0)
				ints[index] = 255;
			else
				ints[index] = 0;
		}
		return ints;
	}

	public static int convertByteArrayToInt(byte[] bytes)
	{
		return (bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | (bytes[3] << 0);

	}

	public static byte[] convertIntToByteArray(int integer)
	{
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (integer >> 24);
		bytes[1] = (byte) ((integer << 8) >> 24);
		bytes[2] = (byte) ((integer << 16) >> 24);
		bytes[3] = (byte) ((integer << 24) >> 24);
		return bytes;
	}
}
