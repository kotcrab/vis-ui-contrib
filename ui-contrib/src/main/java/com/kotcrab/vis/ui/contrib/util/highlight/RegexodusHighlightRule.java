/*
 * Copyright 2014-2016 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.ui.contrib.util.highlight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.highlight.Highlight;
import com.kotcrab.vis.ui.util.highlight.HighlightRule;
import com.kotcrab.vis.ui.widget.HighlightTextArea;
import regexodus.Matcher;
import regexodus.Pattern;

/**
 * Highlighter rule using RegExodus library to detect text matches. Can be used on GWT.
 * @author Kotcrab
 * @since 1.1.2
 */
public class RegexodusHighlightRule implements HighlightRule {
	private Color color;
	private Pattern pattern;

	public RegexodusHighlightRule (Color color, String regex) {
		this.color = color;
		pattern = Pattern.compile(regex);
	}

	@Override
	public void process (HighlightTextArea textArea, Array<Highlight> highlights) {
		Matcher matcher = pattern.matcher(textArea.getText());
		while (matcher.find()) {
			highlights.add(new Highlight(color, matcher.start(), matcher.end()));
		}
	}
}
