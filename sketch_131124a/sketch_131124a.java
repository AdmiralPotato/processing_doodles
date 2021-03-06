/**
 * Created with IntelliJ IDEA.
 * User: admiral
 * Date: 11/23/13
 * Time: 02:49 AM
 * Additive Circles - Actually, Flower Patch
 */

import processing.core.*;
import java.util.*;

public class sketch_131124a extends PApplet {

	boolean
			exportMode = true,
			maskMode = false;

	String modeLabel = "";

	int
			outputScale = 2,
			size = 480,
			half = size / 2,
			currentFrame = 0,
			maxFrames = 48,
			samplesPerFrame = 64,
			preserveAlpha = 0xff000000,
			numberOfThings = 72;
	int[][]
			motionBlurBuffer;

	float
			time = 0,
			motionBlurFactor = 1.0f,
			pi = PI,
			deg = pi / 180,
			tau = pi * 2;

	ArrayList<Ob> obList = new ArrayList<Ob>();
	PImage gradientImage;

	public void setup(){
		size(size * outputScale, size * outputScale, P3D);
		frameRate(24);
		colorMode(HSB, 1.0f, 1.0f, 1.0f, 1.0f);
		smooth(8);
		motionBlurBuffer = new int[width * height][4];
		gradientImage = createImage(width, height, ARGB);
		addCircola(numberOfThings);
	}

	float minMax(float value, float min, float max){
		return max(min(value, max), min);
	}

	//It should be noted that the first 3 args operate in fractions of the width of the window!
	void drawRadialGradient(float xOffset, float yOffset, float maxDistance, int innerColor, int outerColor){
		int
				x, y,
				a, r, g, b, //values per channel when iterating
				colorThisPixel,
				//ripping teh bits apart to get the 0 ~ 255 per channel
				aStart = (innerColor >> 24 & 0xff),
				rStart = (innerColor >> 16 & 0xff),
				gStart = (innerColor >> 8 & 0xff),
				bStart = (innerColor & 0xff),
				aStop = (outerColor >> 24 & 0xff),
				rStop = (outerColor >> 16 & 0xff),
				gStop = (outerColor >> 8 & 0xff),
				bStop = (outerColor & 0xff),
				//getting the difference between the inner and the outer colors
				aDiff = aStart - aStop,
				rDiff = rStart - rStop,
				gDiff = gStart - gStop,
				bDiff = bStart - bStop;
		float
				xDiff, yDiff, distance, distanceFrac,
				//widthOfOnePixelAsFraction fixes a weird problem I was getting with one pixel of the outerColor in the middle???
				//actually, it's 2 pixels to compensate for being radius from center
				widthOfOnePixelAsFraction = 2.0f / ((float) width);
		gradientImage.loadPixels();
		for(y = 0; y < height; y += 1){
			for(x = 0; x < width; x += 1){
				xDiff = ((float) x / width) - xOffset;
				yDiff = ((float) y / width) - yOffset;
				distance = sqrt(xDiff * xDiff + yDiff * yDiff);
				distanceFrac = minMax((distance / maxDistance), widthOfOnePixelAsFraction, 1);
				a = round(minMax((aStart - aDiff * distanceFrac), 0, 255));
				r = round(minMax((rStart - rDiff * distanceFrac), 0, 255));
				g = round(minMax((gStart - gDiff * distanceFrac), 0, 255));
				b = round(minMax((bStart - bDiff * distanceFrac), 0, 255));
				colorThisPixel = //k, let's superglue these channels back into a pixel
						((a & 0xff) << 24) |
								((r & 0xff) << 16) |
								((g & 0xff) << 8) |
								(b & 0xff);
				gradientImage.pixels[y * width + x] = colorThisPixel;
				//println(x, y, xOffset, yOffset, xDiff, yDiff, distanceFrac); //, a, r, g, b
			}
		}
		gradientImage.updatePixels();
		image(gradientImage, 0, 0);
	}


	public void draw(){
		int
				pixelIndex,
				channelIndex,
				sampleIndex;
		pushMatrix();
		//pushing back everything in the scene so it layers behind the radial gradient
		translate(0,0,-40);
		if(samplesPerFrame < 2){
			sample();
		}
		else {
			//set the motionBlurBuffer back to empty
			for (pixelIndex = 0; pixelIndex < width * height; pixelIndex++){
				for (channelIndex = 0; channelIndex < 4; channelIndex++){
					motionBlurBuffer[pixelIndex][channelIndex] = 0;
				}
			}

			for (sampleIndex = 0; sampleIndex < samplesPerFrame; sampleIndex++) {
				time = map(
						currentFrame + ((sampleIndex * motionBlurFactor) / samplesPerFrame), // value
						0, //start1
						maxFrames, //stop1
						0, //start2
						1 //stop2
				);
				sample();
				loadPixels();
				for (int i=0; i<pixels.length; i++) {
					motionBlurBuffer[i][0] += pixels[i] >> 24 & 0xff; //alpha
					motionBlurBuffer[i][1] += pixels[i] >> 16 & 0xff; //red
					motionBlurBuffer[i][2] += pixels[i] >> 8 & 0xff; //green
					motionBlurBuffer[i][3] += pixels[i] & 0xff; //blue
				}
			}

			loadPixels();
			for(pixelIndex = 0; pixelIndex < pixels.length; pixelIndex++){
				pixels[pixelIndex] =
						(motionBlurBuffer[pixelIndex][0]/samplesPerFrame) << 24 | //alpha
								(motionBlurBuffer[pixelIndex][1]/samplesPerFrame) << 16 | //red
								(motionBlurBuffer[pixelIndex][2]/samplesPerFrame) << 8 | //green
								(motionBlurBuffer[pixelIndex][3]/samplesPerFrame); //blue
				//invert and preserveAlpha
				//preserveAlpha = pixels[pixelIndex] >> 24 & 0xff;
				//pixels[pixelIndex] = (~pixels[pixelIndex]) & 0x00ffffff | (preserveAlpha << 24);
			}
			updatePixels();
		}
		popMatrix();
		if(maskMode){
			modeLabel = "-transparent";
		} else {
			drawRadialGradient(0.5f, 0.5f, 1.0f, 0x000000ff, 0x6600cc99);
		}
		if(exportMode){
			saveFrame("pentactus_twisted_" + maxFrames + modeLabel + "/###.png");
		}
		currentFrame++;
		if(exportMode && currentFrame >= maxFrames){
			exit();
		}
		time = (float) currentFrame / maxFrames;
	}

	public void sample(){
		int i, len = obList.size();
		Ob ob;
		if(maskMode){
			clear();
		} else {
			background(0);
		}

		blendMode(ADD);

		pushMatrix();
		scale(outputScale);

		translate(half, half, -100);
		//rotateX(deg * 35);

		//update loop traverses backwards in case anything dies
		for(i = len - 1; i >= 0; i--){
			ob = obList.get(i);
			ob.update();
		}

		//render loop traverses forward for painter's algorithm
		for(i = 0; i < len; i++){
			ob = obList.get(i);
			ob.render();
		}

		popMatrix();
	}


	float sinFrac(float a){
		return (sin(a) / 2) + 0.5f;
	}
	float cosFrac(float a){
		return (cos(a) / 2) + 0.5f;
	}

	void drawEquilateralPolygon(int n, float radius){
		int
				i;
		float
				angle,
				rX, rY,
				frac = 1 / ((float) n);
		beginShape();
		for (i = 0; i < n; i++) {
			angle = tau * frac * i;
			rX = cos(angle) * radius;
			rY = sin(angle) * radius;
			vertex(rX, rY);
		}
		endShape(CLOSE);
	}

	public abstract class Ob{
		int
				offset;
		public float
				x, y, z,
				velX, velY,
				frac, phase,
				angle, speed, curl,
				radius;
		public Ob(){}
		public void update(){}
		public void render(){}
	}


	void addCircola(int n) {
		int i;
		float frac = (float) 1 / n;
		for(i = 0; i < n; i++){
			obList.add(
					new Circola(
							i, //offset
							frac,
							0, //x
							0, //y
							0 //z
					)
			);
		}
	}

	class Circola extends Ob{
		public Circola (int o, float f, float iX, float iY, float iZ) {
			offset = o;
			frac = f;
			x = iX;
			y = iY;
			z = iZ;
			curl = offset * deg;
			radius = (148 * (1 - (frac * (offset + 1)))) + 4;
		}
		public void update() {
			float
					//Binaura twisted: 37
					//Binaura: 36
					//Tricordia: 24
					//Tricordia twisted: 24.4f
					//Clovera: 18
					//Clovera twisted: 18.25f
					//Pentactus: 43.35f
					//??: 16
					//??: 60
					//??: 90
					//??: 120
					//phaseMultiplier = 42f + ((float) mouseX / (float) width * 2),
					phaseMultiplier = 43.35f,
					positionalAngle = (time * tau) + (tau * frac * offset * phaseMultiplier);
			angle = sin(time * tau) * pi * frac * offset;
			x = cos(positionalAngle) * radius;
			y = sin(positionalAngle) * radius;
			z = frac * offset * 120;
		}
		public void render() {
			int
					strokeColor = color(
					((sinFrac(angle / 2.0f) * 0.125f) + 0.4f) % 1.0f, //hue
					1.0f, //saturation
					0.75f, //luminosity
					1 //alpha
			),
					fillColor = color(
							((sinFrac(angle / 2.0f) * 0.125f) + 0.5f) % 1.0f, //hue
							1.0f, //saturation
							((1 - (frac * offset)) * 0.5f) % (1f / 20f), //luminosity
							1 //alpha
					),
					//Pentactus = 10
					//Tricordia: 9
					//Clovera: 8
					//Most of them: 6
					mod = 10,
					numEdges = ((numberOfThings - offset - 1) % mod) + 3;

			pushMatrix();
			translate(x, y, z);
			rotate(angle);

			noStroke();
			fill(fillColor);
			drawEquilateralPolygon(numEdges * 2, radius * 1.0625f);
			//ellipse(0,0, radius * 2, radius * 2);

			noFill();
			stroke(strokeColor);
			strokeWeight(2);
			strokeCap(ROUND);
			strokeJoin(MITER);
			drawEquilateralPolygon(numEdges, radius);
			//ellipse(0,0, radius * 2, radius * 2);

			popMatrix();
		}
	}
}