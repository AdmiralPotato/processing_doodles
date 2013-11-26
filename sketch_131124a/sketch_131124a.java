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

	boolean exportMode = false;

	int
		outputScale = 2,
		size = 480,
		half = size / 2,
		currentFrame = 0,
		maxFrames = 48,
		samplesPerFrame = 32,
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

	public void setup(){
		size(size * outputScale, size * outputScale, P3D);
		frameRate(24);
		colorMode(HSB, 1.0f, 1.0f, 1.0f, 1.0f);
		smooth(8);
		motionBlurBuffer = new int[width * height][4];
		addCircola(numberOfThings);
	}

	public void draw(){
		int
				pixelIndex,
				channelIndex,
				sampleIndex;
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
		if(exportMode){
			saveFrame("0/###.png");
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

		background(0);

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
			float positionalAngle = (time * tau) + (pi * frac * offset * 90);
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
					numEdges = ((numberOfThings - offset) % 9) + 2;

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
