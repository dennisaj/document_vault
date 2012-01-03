package us.paperlesstech

import java.awt.Color

public enum PartyColor {
	Black(new Color(0, 0, 0)),
	Beige(new Color(245, 245, 220)),
	Blue(new Color(0, 0, 255)),
	Cyan(new Color(0, 255, 255)),
	Green(new Color(0, 128, 0)),
	Lime(new Color(0, 255, 0)),
	Magenta(new Color(255, 0, 255)),
	Maroon(new Color(128, 0, 0)),
	Olive(new Color(128, 128, 0)),
	Orange(new Color(255, 165, 0)),
	Purple(new Color(128, 0, 128)),
	Red(new Color(255, 0, 0)),
	Sienna(new Color(168, 82, 45)),
	Yellow(new Color(255, 255, 0))
	
	final Color color
	
	private PartyColor(Color color) {
		this.color = color
	}
	
	String getKey() {
		name()
	}

	@Override
	String toString() {
		name()
	}
}
