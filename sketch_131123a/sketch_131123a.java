/**
 * Created with IntelliJ IDEA.
 * User: admiral
 * Date: 11/23/13
 * Time: 02:49 AM
 * Concentric Hexagon Stacking
 */

import processing.core.*;
import java.util.*;

public class sketch_131123a extends PApplet {

	boolean exportMode = true;

	int
		outputScale = 2,
		size = 480,
		half = size / 2,
		currentFrame = 0,
		maxFrames = 48,
		samplesPerFrame = 32,
		preserveAlpha = 0xff000000;
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
		addHexicle(36);
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

		rotateX(deg * 45);
		translate(half, half, -100);

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

	void drawEqualateralPolygon(int n, float radius){
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


	void addHexicle(int n) {
		int i;
		float frac = (float) 1 / n;
		for(i = 0; i < n; i++){
			obList.add(
					new Hexicle(
							i, //offset
							frac,
							0, //x
							0, //y
							0 //z
					)
			);
		}
	}

	class Hexicle extends Ob{
		public Hexicle (int o, float f, float iX, float iY, float iZ) {
			offset = o;
			frac = f;
			x = iX;
			y = iY;
			z = iZ;
			curl = offset * deg;
			radius = (148 * (1 - (frac * (offset + 1)))) + 4;
		}
		public void update() {
			angle = sin(time * tau) * ((1 - (offset * frac)) * pi);
			z = (offset * frac) + ((1 - (cosFrac(cos((time * tau) + (deg * 90)) * pi * (offset * frac)) * 0.5f)) * 140 * frac * offset);
		}
		public void render() {
			int
					strokeColor = color(
						((sinFrac(angle / 2) * 0.125f) + 0.4f) % 1.0f, //hue
						1.0f, //saturation
						0.5f, //luminosity
						1 //alpha
					),
					fillColor = color(
							((sinFrac(angle / 2) * 0.125f) + 0.5f) % 1.0f, //hue
							1.0f, //saturation
							frac * 1.5f, //luminosity
							1 //alpha
					);

			pushMatrix();
			translate(x, y, z);
			rotate(angle);

			noStroke();
			fill(fillColor);
			drawEqualateralPolygon(6, radius);

			noFill();
			stroke(strokeColor);
			strokeWeight(2);
			strokeCap(ROUND);
			strokeJoin(MITER);
			drawEqualateralPolygon(6, radius);

			popMatrix();
		}
	}
}