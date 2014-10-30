package ccc.java.simpleocr;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.absdiff;
import static org.bytedeco.javacpp.opencv_core.sumElems;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;

import ccc.java.digitextractor.ImageStatics;
import ccc.java.digitextractor.exceptions.MatrixMissmatchException;

public class BinaryPattern extends NativeBinaryImage
{
	private Character character;

	public BinaryPattern(Character chr, int width, int height, byte[] data) throws MatrixMissmatchException
	{
		this.setPattern(ImageStatics.ConvertByteArrayToImage(width, height, CV_8UC1, data));
		this.setWidth(width);
		this.setHeight(height);
		this.character = chr;
	}

	public double CalculateDifference(Mat comp) throws MatrixMissmatchException
	{
		if (comp.size().width() != getWidth() || comp.size().height() != getHeight() || comp.type() != CV_8UC1)
			throw new MatrixMissmatchException("width/ heigth have to be equal, Mat.type() has to be CV_8UC1");

		Mat diffMat = new Mat(getHeight(), getWidth(), CV_8UC1);
		absdiff(getPattern(), comp, diffMat);
		Scalar sumVals = sumElems(diffMat);
		return sumVals.get(0) / (double) (getWidth() * getHeight());
	}

	public Character getCharacter()
	{
		return character;
	}

	public void setCharacter(Character character)
	{
		this.character = character;
	}
}
