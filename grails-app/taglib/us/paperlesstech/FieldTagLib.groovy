package us.paperlesstech

class FieldTagLib {
	static namespace = "pt"

	def textField = { attrs ->
		def disabled = attrs.remove('disabled')
		if (disabled && Boolean.valueOf(disabled)) {
			attrs.disabled = 'disabled'
		}

		out << g.textField(attrs)
	}
}
