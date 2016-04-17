package com.github.czyzby.lml.vis.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.VisUI;

/** Utility content actor for {@link Pin}. Manages an internal {@link Pin} instance. Delegates position setter calls to
 * the pin.
 *
 * @author MJ
 * @see Pin */
public class PinTable extends Table {
    private final Pin<PinTable> pin;

    public PinTable() {
        super(VisUI.getSkin());
        pin = new Pin<PinTable>(this);
    }

    /** @return {@link Pin}, which manages this actor as its content. */
    public Pin<PinTable> getPin() {
        return pin;
    }

    @Override
    public void setX(final float x) {
        pin.setX(x);
    }

    @Override
    public void setY(final float y) {
        pin.setY(y);
    }

    @Override
    public void setPosition(final float x, final float y) {
        pin.setPosition(x, y);
    }

    @Override
    public void setPosition(final float x, final float y, final int alignment) {
        pin.setPosition(x, y, alignment);
    }
}




package com.github.czyzby.lml.vis.parser.impl.tag;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.TableLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.vis.ui.Pin;
import com.github.czyzby.lml.vis.ui.PinTable;

/** Allows to attach a {@link Pin} to a widget. Works similarly to a tooltip: can parse all table attributes and its
 * children can have any cell attributes; can be attached to any widget, even these that normally do not support
 * children. Since Pin extends {@link com.badlogic.gdx.scenes.scene2d.ui.Container}, this tag can have any container
 * attributes as well. Mapped to "pin".
 *
 * @author MJ */
public class PinLmlTag extends TableLmlTag {
    public PinLmlTag(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    public boolean isAttachable() {
        return true;
    }

    @Override
    protected Actor getNewInstanceOfActor(final LmlActorBuilder builder) {
        return new PinTable();
    }

    @Override
    public void attachTo(final LmlTag tag) {
        if (tag.getActor() != null) {
            getPin().setTarget(tag.getActor());
            getParser().addActor(getActor());
        } else {
            getParser().throwErrorIfStrict("Pin can be attached only to valid tags.");
        }
    }

    /** @return internally managed {@link Pin}. */
    protected Pin<PinTable> getPin() {
        return ((PinTable) getActor()).getPin();
    }

    @Override
    protected boolean hasComponentActors() {
        return true;
    }

    @Override
    protected Actor[] getComponentActors(final Actor actor) {
        return new Actor[] { ((PinTable) actor).getPin() };
    }
}





package com.github.czyzby.lml.vis.parser.impl.tag.provider;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;
import com.github.czyzby.lml.vis.parser.impl.tag.PinLmlTag;

/** Provides pin tags.
 *
 * @author MJ */
public class PinLmlTagProvider implements LmlTagProvider {
    @Override
    public LmlTag create(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        return new PinLmlTag(parser, parentTag, rawTagData);
    }
}


    
    


package com.github.czyzby.lml.vis.parser.impl.attribute.pin;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.vis.ui.Pin;
import com.github.czyzby.lml.vis.ui.PinTable;

/** See {@link Pin#setAbsolutePosition(boolean)}. Mapped to "absolute", "absolutePosition".
 *
 * @author MJ */
public class AbsolutePositionLmlAttribute implements LmlAttribute<PinTable> {
    @Override
    public Class<PinTable> getHandledType() {
        return PinTable.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final PinTable actor, final String rawAttributeData) {
        actor.getPin().setAbsolutePosition(parser.parseBoolean(rawAttributeData, actor.getPin()));
    }
}




package com.github.czyzby.lml.vis.parser.impl.attribute.pin;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.vis.ui.Pin;
import com.github.czyzby.lml.vis.ui.PinTable;

/** See {@link Pin#setMimicSize(boolean)}. Mapped to "mimic", "mimicSize".
 *
 * @author MJ */
public class MimicSizeLmlAttribute implements LmlAttribute<PinTable> {
    @Override
    public Class<PinTable> getHandledType() {
        return PinTable.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final PinTable actor, final String rawAttributeData) {
        actor.getPin().setMimicSize(parser.parseBoolean(rawAttributeData, actor.getPin()));
    }
}



/* Attribute registration - add to VisLmlSyntax:
    protected void registerPinAttributes() {
        addAttributeProcessor(new AbsolutePositionLmlAttribute(), "absolute", "absolutePosition");
        addAttributeProcessor(new MimicSizeLmlAttribute(), "mimic", "mimicSize");
    }
*/
