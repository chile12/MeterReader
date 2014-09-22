package ccc.java.digitextractor;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_highgui.imdecode;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;

import ccc.java.digitextractor.data.BackGroundShade;
import ccc.java.digitextractor.data.MatchingCharacter;
import ccc.java.digitextractor.data.RectExt;
import ccc.java.digitextractor.exceptions.FrameNotFoundException;
import ccc.java.simpleocr.BinaryPatternContainer;
import ccc.java.simpleocr.DigitOcr;
import ccc.java.stringalignement.MultipleSequenceAliner;

public class DigitReadingProcessor
{
	public static int CAMERA_ORIENT_LANDSCAPE = 0;
	public static int CAMERA_ORIENT_PORTRAIT = 1;

	// private List<MatchingCharacter> anchorRowA;
	// private int anchorRowAStartPos;
	// private List<MatchingCharacter> anchorRowB;
	// private int anchorRowBStartPos;
	// private List<int[][]> scoreBoard = new ArrayList<int[][]>();
	// private List<LinkedHashMap<Integer, Integer>> evaluatedScores = new
	// ArrayList<LinkedHashMap<Integer, Integer>>();
	// private List<List<MatchingCharacter>> firstPrecs = new
	// ArrayList<List<MatchingCharacter>>();
	// private LinkedHashMap<Integer, Integer> positions = new
	// LinkedHashMap<Integer, Integer>();
	// private List<List<MatchingCharacter>> precisions = new
	// ArrayList<List<MatchingCharacter>>();
	// private List<List<Double>> alignments = new ArrayList<List<Double>>();
	// private List<NeedlemanWunsch> nws = new ArrayList<NeedlemanWunsch>();

	private MultipleSequenceAliner aligner;

	private List<MeterImage> readings = new ArrayList<MeterImage>();
	private DigitOcr standardDigitOcr;
	private int imageFormat = 17; // NV21 = 17 -> android.graphics.ImageFormat
	private int cameraOrientation = CAMERA_ORIENT_LANDSCAPE;
	private Rect standardCropFrame;
	private Runnable doAutoFocus;

	private int lastEvaluatedReading = -1;
	private int currentDigitCount;
	private int currentDecimals;
	private BackGroundShade currentShade;
	private MeterImage current;

	private String currentResult;

	public DigitReadingProcessor(BinaryPatternContainer standardPatterns)
	{
		standardDigitOcr = new DigitOcr(standardPatterns);
	}

	public DigitReadingProcessor(BinaryPatternContainer standardPatterns, int imageFormat)
	{
		standardDigitOcr = new DigitOcr(standardPatterns);
		this.imageFormat = imageFormat;
	}

	public DigitReadingProcessor(BinaryPatternContainer standardPatterns, int imageFormat, int x, int y, int w, int h)
	{
		standardDigitOcr = new DigitOcr(standardPatterns);
		this.imageFormat = imageFormat;
		this.standardCropFrame = new Rect(x, y, w, h);
	}

	private void setBackCurrents()
	{
		// scoreBoard.clear();
		readings.clear();
		currentDigitCount = -1;
		currentDecimals = -1;
		currentShade = BackGroundShade.Undef;
	}

	public void initializeReadingSession()
	{
		setBackCurrents();
	}

	// public void initializeReadingSession(BackGroundShade shade) {
	// setBackCurrents();
	// currentShade = shade;
	// }

	public void initializeReadingSession(int digitCount)
	{
		setBackCurrents();
		currentDigitCount = digitCount;
		aligner = new MultipleSequenceAliner(digitCount);
	}

	public void initializeReadingSession(int digitCount, int decimals)
	{
		setBackCurrents();
		currentDigitCount = digitCount;
		currentDecimals = decimals;
		aligner = new MultipleSequenceAliner(digitCount);
	}

	public void initializeReadingSession(BackGroundShade shade, int digitCount, int decimals)
	{
		setBackCurrents();
		currentShade = shade;
		currentDigitCount = digitCount;
		currentDecimals = decimals;
		aligner = new MultipleSequenceAliner(digitCount);
	}

	public boolean addReadingImage(byte[] data, int width, int height)
	{
		if (standardCropFrame != null)
			return addReadingImage(data, width, height, standardCropFrame);
		return false;
	}

	public boolean addReadingImage(byte[] data, int width, int height, boolean findDisplayautomatically)
	{
		return false;
	}

	public boolean addReadingImage(byte[] data, int width, int height, int x, int y, int w, int h)
	{
		return addReadingImage(data, width, height, new Rect(x, y, w, h));
	}

	public boolean addReadingImage(String filePath, boolean findDisplayautomatically)
	{
		Mat img = imread(filePath);
		Rect frame = null;
		if (findDisplayautomatically)
			try
			{
				frame = getDigitFrameBySignalColor(img);
			}
			catch (FrameNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return addReadingImage(img, frame);
	}

	public boolean addReadingImage(byte[] data, int width, int height, Rect frame)
	{
		standardDigitOcr.clearResults();

		Mat bgra = null;

		if (this.imageFormat == 17)
			bgra = createMatFromNv21(data, width, height);
		else if (this.imageFormat == 256)
			bgra = createMatFromJpeg(data, width, height);

		return addReadingImage(bgra, frame);
	}

	public boolean addReadingImage(Mat raw, Rect frame)
	{
		if (readings.size() > 0)
			this.readings.get(0).release();
		if (frame != null)
			raw = ImageStatics.CropImage(raw, frame);
		if (cameraOrientation == CAMERA_ORIENT_PORTRAIT)
			raw = rotateRight(raw);
		MeterImage img = new MeterImage(raw, currentDigitCount, currentShade, standardDigitOcr, false);

		current = img;
		readings.add(0, img);

		return true;
	}

	private Mat createMatFromNv21(byte[] data, int width, int height)
	{
		Mat bgra = new Mat(height, width, org.bytedeco.javacpp.opencv_core.CV_8UC3);
		Mat nv21 = new Mat(height + (height / 2), width, org.bytedeco.javacpp.opencv_core.CV_8UC1);
		nv21.getByteBuffer().put(data);
		org.bytedeco.javacpp.opencv_imgproc.cvtColor(nv21, bgra, org.bytedeco.javacpp.opencv_imgproc.CV_YUV2BGR_NV21);
		return bgra;
	}

	private Mat createMatFromJpeg(byte[] data, int width, int height)
	{
		Mat zw = new Mat(width, height, 1);
		zw.getByteBuffer().put(data);
		Mat jpeg = imdecode(zw, 1);
		return jpeg;
	}

	private Mat rotateRight(Mat bgra)
	{
		Mat trans = new Mat(bgra.size().height(), bgra.size().width(), bgra.type());
		org.bytedeco.javacpp.opencv_core.transpose(bgra, trans);
		bgra = new Mat(bgra.size().width(), bgra.size().height(), bgra.type());
		org.bytedeco.javacpp.opencv_core.flip(trans, bgra, 1);
		return bgra;
	}

	public void processLastEntry()
	{
		if (readings.size() > 0)
			try
			{

				current.ExtractDigits();

				List<MatchingCharacter> matches = new ArrayList<MatchingCharacter>();

				for (Integer chr : this.standardDigitOcr.getPreciseChars().keySet())
					matches.add(this.standardDigitOcr.getPreciseChars().get(chr));

				aligner.addMatchingCharacterList(matches);
			}
			catch (FrameNotFoundException e)
			{
				e.printStackTrace();
			}
	}

	private Rect getDigitFrameBySignalColor(Mat raw) throws FrameNotFoundException
	{

		Mat hsvImage = new Mat(raw.size(), CV_8UC3);
		cvtColor(raw, hsvImage, CV_BGR2HSV);
		Mat dotThresHold = ImageStatics.getHsvColorMask(hsvImage, ImageStatics.getOpenCvHSV(ImageStatics.DIGIT_SIGNAL_VAL_RGB_H_MIN,
				ImageStatics.DIGIT_SIGNAL_VAL_RGB_S_MIN, ImageStatics.DIGIT_SIGNAL_VAL_RGB_V_MIN), ImageStatics.getOpenCvHSV(
				ImageStatics.DIGIT_SIGNAL_VAL_RGB_H_MAX, ImageStatics.DIGIT_SIGNAL_VAL_RGB_S_MAX, ImageStatics.DIGIT_SIGNAL_VAL_RGB_V_MAX));
		List<RectExt> dots = RectExt.ConvertCvRectList(ImageStatics.GetContoursOfImage(dotThresHold), 0);
		if (dots.size() == 0)
			throw new FrameNotFoundException("no frame found");
		int minCoordsAddition = 0;
		Rect minCoords = null;
		int maxCoordsAddition = 0;
		Rect maxCoords = null;

		for (int i = 0; i < dots.size(); i++)
		{
			Rect zw = dots.get(i);
			if (maxCoordsAddition < (zw.x() + zw.y()))
			{
				maxCoordsAddition = zw.x() + zw.y();
				maxCoords = zw;
			}
			if (minCoordsAddition == 0 || minCoordsAddition > (zw.x() + zw.y()))
			{
				minCoordsAddition = zw.x() + zw.y();
				minCoords = zw;
			}
		}
		Rect frame = new Rect(minCoords.x() + (minCoords.width() / 2), minCoords.y() + (minCoords.height() / 2), (maxCoords.x() - minCoords.x()),
				(maxCoords.y() - minCoords.y()));
		return frame;
	}

	public void EvaluateReadings()
	{

	}

	public int getReadingCount()
	{
		return readings.size();
	}

	public Runnable getDoAutoFocus()
	{
		return doAutoFocus;
	}

	public void setDoAutoFocus(Runnable doAutoFocus)
	{
		this.doAutoFocus = doAutoFocus;
	}

	public MeterImage getCurrent()
	{
		return current;
	}

	public int getCameraOrientation()
	{
		return cameraOrientation;
	}

	public void setCameraOrientation(int cameraOrientation)
	{
		this.cameraOrientation = cameraOrientation;
	}

	public MultipleSequenceAliner getAligner()
	{
		return aligner;
	}

	public void alignResults()
	{
		aligner.alignResults();
	}
}
