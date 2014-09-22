package ccc.java.digitextractor.data;

import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;

public class MatchingCharacter
{
	private Character character;
	private int positionInCluster;
	private Mat img;
	private double precision;
	private int position;

	public MatchingCharacter(Character chr, int position, double precision)
	{
		this.character = chr;
		this.positionInCluster = position;
		this.precision = precision;
	}

	public MatchingCharacter(Character chr, Mat img, double precision)
	{
		this.character = chr;
		this.img = img;
		this.precision = precision;
	}

	public Character getCharacter()
	{
		return character;
	}

	public void setCharacter(Character character)
	{
		this.character = character;
	}

	public int getPositionInCluster()
	{
		return positionInCluster;
	}

	public void setPositionInCluster(int positionInCluster)
	{
		this.positionInCluster = positionInCluster;
	}

	public double getPrecision()
	{
		return precision;
	}

	public Mat getImg()
	{
		return img;
	}

	public void setImg(Mat img)
	{
		this.img = img;
	}

	public void setPrecision(double precision)
	{
		this.precision = precision;
	}

	public int getPosition()
	{
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	@Override
	public String toString()
	{
		String value = String.valueOf(this.precision);
		return this.character.toString() + " - " + value.substring(0, Math.min(value.length(), 5));
	}

	public static String extractString(List<MatchingCharacter> list)
	{
		String ret = "";
		for (MatchingCharacter chr : list)
			if (chr == null)
				ret += "-";
			else
				ret += chr.getCharacter();
		while (ret.length() > 0 && ret.charAt(0) == '-')
		{
			ret = ret.substring(1);
		}
		while (ret.length() > 0 && ret.charAt(ret.length() - 1) == '-')
		{
			ret = ret.substring(0, ret.length() - 1);
		}
		return ret;
	}
}
