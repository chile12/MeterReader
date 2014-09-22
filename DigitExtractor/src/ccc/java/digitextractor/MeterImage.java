package ccc.java.digitextractor;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_core.bitwise_or;
import static org.bytedeco.javacpp.opencv_core.minMaxLoc;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.BORDER_DEFAULT;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.Laplacian;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;

import ccc.java.digitextractor.data.BackGroundShade;
import ccc.java.digitextractor.exceptions.FrameNotFoundException;
import ccc.java.digitextractor.statichelpers.ParallelExecuter;
import ccc.java.simpleocr.DigitOcr;

public class MeterImage
{
	private BackGroundShade shade;
	private Mat original;
	private Mat grayScale;
	private Mat hsvImage;
	private OpticalDigitRecognizer odr;
	private int digitCount;
	private boolean isPrepared = false;
	private boolean replaceLowContrastColors = false;
	private String extractedbestvalues;
	private String extractedavgvalues;
	private DigitOcr standardOcr;
	private String preciseChars = "";

	public String getExtractedBestValues()
	{
		return extractedbestvalues;
	}

	public MeterImage(int width, int height, int digits, BackGroundShade shade, DigitOcr standardOcr, boolean replaceLowContrastColors)
	{
		ParallelExecuter.initialize();
		this.original = new Mat(width, height, CV_8UC3);
		this.grayScale = new Mat(width, height, CV_8UC1);
		this.hsvImage = new Mat(original.size(), CV_8UC3);
		this.original.setTo(new Mat(Scalar.all(0)));
		this.grayScale.setTo(new Mat(Scalar.all(0)));
		this.digitCount = digits;
		this.shade = shade;
		this.replaceLowContrastColors = replaceLowContrastColors;
		cvtColor(original, hsvImage, CV_BGR2HSV);
		prepareImage();
		this.standardOcr = standardOcr;
		if (isPrepared)
		{
			odr = new OpticalDigitRecognizer(this.grayScale, digits, shade, standardOcr);
		}
	}

	// special internal constructor
	protected MeterImage(Mat original)
	{
		ParallelExecuter.initialize();
		this.grayScale = original;
	}

	public MeterImage(Mat original, int digits, BackGroundShade shade, DigitOcr standardOcr, boolean replaceLowContrastColors)
	{
		ParallelExecuter.initialize();
		this.digitCount = digits;
		this.original = original;
		this.standardOcr = standardOcr;
		this.digitCount = digits;
		this.shade = shade;
		this.replaceLowContrastColors = replaceLowContrastColors;
		prepareImage();
		if (isPrepared)
		{
			odr = new OpticalDigitRecognizer(this.grayScale, digits, shade, standardOcr);
		}
	}

	public MeterImage(String filePath, int digits, BackGroundShade shade, DigitOcr standardOcr, boolean replaceLowContrastColors)
	{
		ParallelExecuter.initialize();
		this.original = ImageStatics.GetDigitIplFromFile(filePath);
		this.grayScale = new Mat(original.size(), CV_8UC1);
		this.hsvImage = new Mat(original.size(), CV_8UC3);
		this.digitCount = digits;
		this.standardOcr = standardOcr;
		this.shade = shade;
		this.replaceLowContrastColors = replaceLowContrastColors;
		cvtColor(original, hsvImage, CV_BGR2HSV);
		prepareImage();
		if (isPrepared)
		{
			odr = new OpticalDigitRecognizer(this.grayScale, digits, shade, standardOcr);
		}
	}

	public void release()
	{
		ParallelExecuter.shutdown();
		this.original.release();
		this.grayScale.release();
		if (hsvImage != null)
			this.hsvImage.release();
	}

	public ByteBuffer getImageBuffer()
	{
		return original.getByteBuffer();
	}

	private void prepareImage()
	{
		// if(ImageStatics.DEV_MODE)
		// {
		// Rect frame = null;
		// try {
		// frame = getDigitFrameBySignalColor();
		// } catch (FrameNotFoundException nff) {
		// return;
		// }
		// this.original = ImageStatics.CropImage(this.original, frame);
		// this.hsvImage = ImageStatics.CropImage(this.hsvImage, frame);
		// }
		this.grayScale = new Mat(original.size(), CV_8UC1);
		if (replaceLowContrastColors)
			replaceProblemColor();
		cvtColor(original, this.grayScale, CV_RGB2GRAY);
		imwrite(ImageStatics.ROOT + "\\graay.jpg", this.grayScale);

		// TODO display detection with houghlines maybe add corner detection
		// blur(this.grayScale, this.grayScale, new Size(19,19), new Point(-1,
		// -1), BORDER_DEFAULT);
		//
		// Mat lines = new Mat(0, 1, CV_64F);
		// int threshold = 33;
		// int minLineSize = 5;
		// int lineGap = 50;
		//
		// for(int i = ImageStatics.BRIGHTNESS_STEP_DISTANCE; i < 128; i = i +
		// ImageStatics.BRIGHTNESS_STEP_DISTANCE)
		// {
		// Mat houghlines = new Mat();
		// Mat zw = ImageStatics.ChangeBrightnessContrast(grayScale.clone(), i,
		// 128);
		// Canny(zw , zw, 10, 45);
		// imwrite(ImageStatics.ROOT + "\\canny.jpg", zw);
		// HoughLinesP(zw, houghlines, 1, Math.PI / 180, threshold, minLineSize,
		// lineGap);
		// double[] vec = new double[4];
		//
		// for (int x = 0; x < houghlines.cols(); x=x+4)
		// {
		// houghlines.asCvMat().get(x, vec);
		//
		// double x1 = vec[0],
		// y1 = vec[1],
		// x2 = vec[2],
		// y2 = vec[3];
		// Point start = new Point();
		// start.x((int) x1);
		// start.y((int) y1);
		// Point end = new Point();
		// end.x((int) x2);
		// end.y((int) y2);
		//
		// if(isImLot(start, end, original.size()))
		// line(original, start, end, new Scalar(255, 0, 0, 0), 3, 8, 0);
		//
		// }
		// }
		// imwrite(ImageStatics.ROOT + "\\lines.jpg", original);
		// TODO check this
		isPrepared = true;
	}

	private boolean isImLot(Point a, Point b, Size s)
	{
		double dist = Math.sqrt(Math.pow(Math.abs((double) a.y() - (double) b.y()), 2d) + Math.pow(Math.abs((double) a.x() - (double) b.x()), 2d));
		double tolleranceX = ((dist / (double) s.width()) * s.width() * 0.05);
		double tolleranceY = ((dist / (double) s.height()) * s.width() * 0.05);

		if (Math.abs(a.x() - b.x()) < tolleranceX || Math.abs(a.y() - b.y()) < tolleranceY)
			return true;
		return false;

	}

	private void replaceProblemColor()
	{
		Scalar min1 = ImageStatics.getOpenCvHSV(ImageStatics.LOW_CONTRAST_COLOR1_RGB_H_MIN, ImageStatics.LOW_CONTRAST_COLOR1_RGB_S_MIN,
				ImageStatics.LOW_CONTRAST_COLOR1_RGB_V_MIN);
		Scalar max1 = ImageStatics.getOpenCvHSV(ImageStatics.LOW_CONTRAST_COLOR1_RGB_H_MAX, ImageStatics.LOW_CONTRAST_COLOR1_RGB_S_MAX,
				ImageStatics.LOW_CONTRAST_COLOR1_RGB_V_MAX);
		Mat mask1 = ImageStatics.getHsvColorMask(this.hsvImage, min1, max1);
		Scalar min2 = ImageStatics.getOpenCvHSV(ImageStatics.LOW_CONTRAST_COLOR2_RGB_H_MIN, ImageStatics.LOW_CONTRAST_COLOR2_RGB_S_MIN,
				ImageStatics.LOW_CONTRAST_COLOR2_RGB_V_MIN);
		Scalar max2 = ImageStatics.getOpenCvHSV(ImageStatics.LOW_CONTRAST_COLOR2_RGB_H_MAX, ImageStatics.LOW_CONTRAST_COLOR2_RGB_S_MAX,
				ImageStatics.LOW_CONTRAST_COLOR2_RGB_V_MAX);
		Mat mask2 = ImageStatics.getHsvColorMask(this.hsvImage, min2, max2);
		bitwise_or(mask1, mask2, mask1);
		imwrite(ImageStatics.ROOT + "\\mask.jpg", mask1);
		try
		{
			if (this.shade == BackGroundShade.Bright)
				original.setTo(new Mat(Scalar.all(0)), mask1);
			else if (this.shade == BackGroundShade.Dark)
				original.setTo(new Mat(Scalar.all(255)), mask1);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imwrite(ImageStatics.ROOT + "\\replaced.jpg", this.original);
	}

	// private CvRect automaticallyDetectDigitFrame() throws
	// FrameNotFoundException
	// {
	// List<CvRect> boxes = findRectanglesInImage();
	// Mat ze = ImageStatics.DrawRectanglesIntoImage(this.original, boxes,
	// CvScalar.RED, 5);
	// cvSaveImage(ImageStatics.ROOT + "\\edges.jpg", ze);
	// return boxes.get(0);
	// }
	//

	public List<byte[]> ExtractDigits() throws FrameNotFoundException
	{
		Date d = new Date();
		final List<List<Mat>> digits = odr.ExtractDigits();
		// TODO
		if (digits == null)
			return null;

		List<Mat> output = new ArrayList<Mat>();
		extractedbestvalues = "";
		extractedavgvalues = "";
		// /DEBUG
		if (ImageStatics.DEV_MODE)
		{
			for (int i = 0; i < digits.size(); i++)
				ImageStatics.putOutDigitsAsFiles(digits.get(i), ImageStatics.ROOT + "\\posiDigits\\" + String.valueOf(i), null);
		}
		// /END DEBUG
		// ParallelExecuter.loop(0, digits.size(), new Looper()
		// {
		// @Override
		// public void loop(int j, int to, int looperID) {
		// {

		standardOcr.clearResults();
		for (int j = 0; j < digits.size(); j++)
		{
			// TODO
			// use Character Array to select other possible digits (sorted in
			// order of accuracy)
			if (digits.get(j) == null)
			{
				extractedbestvalues += '-';
				preciseChars += "-";
				continue;
			}
			Map<Character, Integer> evalbestChars = standardOcr.EvaluateDigitClusterBest(digits.get(j), j);
			if (evalbestChars.size() > 0)
				extractedbestvalues += evalbestChars.keySet().toArray()[0];
			else
				extractedbestvalues += '-';
			if (standardOcr.getPreciseChars().get(j) == null)
				preciseChars += "-";
			else
				preciseChars += standardOcr.getPreciseChars().get(j).getCharacter();

			// Map<Character, Double> evalavgChars =
			// standardOcr.EvaluateDigitClusterAvg(digits.get(j), j);
			// if(evalbestChars.size() >0)
			// extractedavgvalues += evalbestChars.keySet().toArray()[0];
			// else
			// extractedavgvalues += '-';

		}
		// }});

		if (extractedavgvalues.length() > digitCount)
			extractedavgvalues = extractedavgvalues.substring(0, digitCount);
		if (extractedbestvalues.length() > digitCount)
			extractedbestvalues = extractedbestvalues.substring(0, digitCount);

		for (int i = 0; i < extractedbestvalues.length(); i++)
		{
			Character chr = extractedbestvalues.charAt(i);
			if (chr != '-')
			{
				output.add(digits.get(i).get(standardOcr.getPreciseChars().get(i).getPositionInCluster()));
			}
			else
				output.add(null);
		}
		// DEBUG
		if (ImageStatics.DEV_MODE)
		{
			ImageStatics.putOutDigitsAsFiles(output, ImageStatics.ROOT + "\\output", null);
		}
		// ENDDEBUG
		System.out.println(new Date().getTime() - d.getTime());
		return ImageStatics.ConvertImagesToByteArrays(output);
	}

	public double GetSharpness()
	{
		Mat res = new Mat(grayScale.size(), grayScale.type());
		Laplacian(grayScale, res, grayScale.depth(), 1, 1, 0, BORDER_DEFAULT);
		double[] min_val = new double[2];
		double[] max_val = new double[2];
		minMaxLoc(res, min_val, max_val, new Point(), new Point(), new Mat());
		return max_val[0];
	}

	public String getResult()
	{
		if (extractedavgvalues == null)
			return "";
		String output = extractedavgvalues;
		for (int i = 0; i < extractedavgvalues.length(); i++)
		{
			if (this.preciseChars.charAt(i) == this.extractedbestvalues.charAt(i))
			{
				output = output.substring(0, i) + this.preciseChars.charAt(i);
				if (i + 1 < preciseChars.length())
					output += extractedavgvalues.substring(i + 1);
			}
		}
		return output;
	}

	public Mat getOriginal()
	{
		return original;
	}

	public void setOriginal(Mat original)
	{
		this.original = original;
	}

	public int getDigits()
	{
		return digitCount;
	}

	public void setDigits(int digits)
	{
		this.digitCount = digits;
	}

	public Mat getGrayScale()
	{
		return grayScale;
	}

	public void setGrayScale(Mat grayScale)
	{
		this.grayScale = grayScale;
	}

	public String getExtractedavgvalues()
	{
		return extractedavgvalues;
	}

	public String getPreciseChars()
	{
		return preciseChars;
	}

	public boolean isReplaceLowContrastColors()
	{
		return replaceLowContrastColors;
	}

	public void setReplaceLowContrastColors(boolean replaceLowContrastColors)
	{
		this.replaceLowContrastColors = replaceLowContrastColors;
	}
}
