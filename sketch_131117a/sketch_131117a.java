/**
 * Created with IntelliJ IDEA.
 * User: admiral
 * Date: 11/17/13
 * Time: 05:49 AM
 * No idea yet!
 */

import java.lang.Math;
import processing.core.*;
import java.util.*;

public class sketch_131117a extends PApplet {
	ArrayList<Ob> obList = new ArrayList<Ob>();
	int
			//change outputScale to 2 for final export, then use an image editor to scale down for better antialiasing
			outputScale = 1,
			windowSize = 480 * outputScale,
			half = (int) windowSize / 2,
			maxFrames = 60,
			frameNum = 0;

	double
			pi = Math.PI,
			tau = pi * 2,
			deg = pi / 180;

	double sinFrac(double r){
		return (double) (Math.sin(r) / 2) + 0.5;
	}
	double cosFrac(double r){
		return (double) (Math.sin(r) / 2) + 0.5;
	}

	//Set to fullscreen if app has problems launching - this seems to increase rate of successful sketch launch;
	//On macs, it seems to have problems initializing the rendering context larger than 512px
	public boolean sketchFullScreen() {
		return false;
	}

	public void setup() {
		int
				i, j,
				n = 6,
				spacing = windowSize / n;

		size(windowSize, windowSize, P2D);
		frameRate(24);

		addHexicle(10, 0, 6, 4);
	}

	public void draw() {
		int i, len = obList.size();
		Ob ob;
		background(0x000000);
		//clear();
		colorMode(HSB, 1, 1, 1, 1);
		translate(half, half);
		scale(outputScale);
		smooth(8);
		ellipseMode(RADIUS);	// Set ellipseMode to RADIUS
		noFill();
		stroke(0, 1, 1);
		blendMode(SCREEN);

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

		//saveFrame("0/###.png");
		//frameNum++;
		//if(frameNum >= maxFrames){
		//	noLoop();
		//	exit();
		//}
	}

	public abstract class Ob{
		public void update(){}
		public void render(){}
	}


	void addHexicle(int n, double phase, double curl, double speed) {
		int i;
		double frac = 1 / ((double) n);
		println("frac:", frac);
		for(i = 0; i < n; i++){
			obList.add(
					new Hexicle(
							i, //offset
							frac, //angle
							phase, //phase
							0, //x
							0, //y
							curl, //curl
							speed * frac * i, //speed
							240 * frac * i
					)
			);
		}
	}

	class Hexicle extends Ob{
		int
				offset;
		double
				x, y,
				velX, velY,
				frac, phase,
				angle, speed, curl,
				radius;
		public Hexicle (int o, double f, double p, double iY, double iX, double c, double s, double r) {
			offset = o;
			frac = f;
			phase = p;
			angle = ((offset * frac) + phase) * tau;
			//println(offset, frac, angle);

			x = iX;
			y = iY;
			speed = 0; //s;
			curl = c * deg;
			radius = r;
		}
		public void rot() {
			velX = Math.cos(angle) * speed;
			velY = Math.sin(angle) * speed;
		}
		public void wrap() {
			//if it's offscreen, put it back on in the middle
			if(Math.abs(x) > half || Math.abs(y) > half){
				x = (Math.abs(x) - half) * Math.signum(x);
				y = (Math.abs(y) - half) * Math.signum(y);
			}
		}
		public void update() {
			//angle += curl;
			//rot();
			curl += deg;
			angle = Math.sin(curl) * (pi + (offset * frac * pi));
			//x += velX;
			//y += velY;
			wrap();
		}
		public void render() {
			int i;
			double r;
			float rX, rY;
			stroke(
					(float) ((sinFrac(angle) * 0.0125) + 0.7) % 1, //hue
					1.0f, //saturation
					0.75f, //luminosity
					1 //alpha
			);
			fill(
					(float) ((sinFrac(angle) * 0.0125) + 0.7) % 1, //hue
					1.0f, //saturation
					0.5f, //luminosity
					1 //alpha
			);
			strokeWeight(2);
			strokeCap(ROUND);
			strokeJoin(MITER);


			pushMatrix();
			translate((float) x, (float) y);
			rotate((float) angle);
			beginShape();
			for (i = 0; i < 6; i++) {
				r =  deg * 60 * i;
				rX = (float) (Math.cos(r) * radius);
				rY = (float) (Math.sin(r) * radius);
				vertex(rX, rY);
			}
			endShape(CLOSE);
			popMatrix();


//			line(
//					(float) x,
//					(float) y,
//					(float) (x - (velX * circleRadius * 0.5)),
//					(float) (y - (velY * circleRadius * 0.5))
//			);
		}
	}

}
