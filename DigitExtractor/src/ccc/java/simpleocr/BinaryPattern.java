package ccc.java.simpleocr;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.absdiff;
import static org.bytedeco.javacpp.opencv_core.sumElems;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;

import ccc.java.digitextractor.ImageStatics;
import ccc.java.digitextractor.exceptions.MatrixMissmatchException;

public class BinaryPattern
{
	private Mat pattern;
	private int width;
	private int height;
	private Character character;

	public BinaryPattern(Character chr, int width, int height, byte[] data) throws MatrixMissmatchException
	{
		pattern = ImageStatics.ConvertByteArrayToImage(width, height, CV_8UC1, data);
		this.width = width;
		this.height = height;
		this.character = chr;
	}

	public double CalculateDifference(Mat comp) throws MatrixMissmatchException
	{
		if (comp.size().width() != width || comp.size().height() != height || comp.type() != CV_8UC1)
			throw new MatrixMissmatchException("width/ heigth have to be equal, Mat.type() has to be CV_8UC1");

		Mat diffMat = new Mat(height, width, CV_8UC1);
		absdiff(pattern, comp, diffMat);
		Scalar sumVals = sumElems(diffMat);
		return sumVals.get(0) / (double) (width * height);
	}

	public Mat getPattern()
	{
		return pattern;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public Character getCharacter()
	{
		return character;
	}

	public void setPattern(Mat pattern)
	{
		this.pattern = pattern;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public void setCharacter(Character character)
	{
		this.character = character;
	}
}
