package com.kotcrab.vis.ui.contrib.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

/** Allows to display a single widget attached to an actor. Pins are not rendered unless an attachment target is set
 * with the constructor or {@link #setTarget(Actor)}. They should not be added to groups (like table); instead, add them
 * directly to stages. Modify pin's behavior with {@link #setAbsolutePosition(boolean)} and
 * {@link #setMimicSize(boolean)}. While pins can use any actors as their content, flexible and highly configurable
 * widgets like {@link Table} are advised to be used - especially if you need custom settings like offsets or paddings.
 *
 * <p>
 * Note that by default pins are {@link Touchable#disabled} and will not receive any input. Pins also will NOT copy
 * target's specific data. While they can mimic simple settings like position or size, they will not copy target's
 * rotation.
 *
 * <p>
 * This can be a relatively "heavy" widget, as there are no position change listeners on most widgets and position
 * calculations have to be done on every render call. Use a pin only when necessary; prefer regular groups'
 * customization method where possible.
 *
 * <p>
 * Note that some actors report their data differently and {@link Pin} position might need some adjustments.
 *
 * @author MJ
 *
 * @param <Content> type of content of the pin.
 * @see #setTouchable(Touchable)
 * @see Container */
public class Pin<Content extends Actor> extends Actor implements Layout {
    protected final Vector2 temp = new Vector2();
    private final Content content;
    private Actor target;
    private PositionExtractor positionExtractor = PositionExtractor.RELATIVE;
    private SizeExtractor sizeExtractor = SizeExtractor.ABSOLUTE;
    private boolean keepWithinStage = true;
    private float cachedX, cachedY;

    /** @param content becomes the content widget of the pin. Rendered only if a target is present. */
    public Pin(final Content content) {
        this(content, null);
    }

    /** @param content becomes the content widget of the pin. Rendered only if a target is present.
     * @param target pin will be attached to this actor. */
    public Pin(final Content content, final Actor target) {
        this.content = content;
        content.setOrigin(0f, 0f);
        setTarget(target);
    }

    {
        setTouchable(Touchable.disabled);
    }

    /** @return the actor that the pin is currently attached to. Can be null. */
    public Actor getTarget() {
        return target;
    }

    /** @param target becomes the actor that the pin is attached to. Can be null. */
    public void setTarget(final Actor target) {
        this.target = target;
    }

    @Override
    public void act(final float delta) {
        super.act(delta);
        content.act(delta);
        toFront();
    }

    @Override
    public void draw(final Batch batch, final float parentAlpha) {
        if (target != null) {
            sizeExtractor.setSize(this);
            positionExtractor.beforeDraw(this);
            content.setPosition(getX(), getY());
            content.draw(batch, parentAlpha * getColor().a);
            positionExtractor.afterDraw(this);
        }
    }

    /** @param mimic if true, pin will always match width and height returned by its target. If false (default), size
     *            set with {@link #setSize(float, float)} (and similar methods) will be honored. Note that if this
     *            setting is changed to true, size will be validated and updated every frame, as there are no size
     *            change listeners in most actors. */
    public void setMimicSize(final boolean mimic) {
        sizeExtractor = mimic ? SizeExtractor.MIMIC : SizeExtractor.ABSOLUTE;
    }

    /** Setting position to absolute is useful for caching pin's position if you don't want it validated every frame:
     * use {@link Actor#localToStageCoordinates(Vector2)} method to determine actual target's position and set it once
     * with {@link #setPosition(float, float)} method to avoid unnecessary calculations. Pin will behave perfectly fine
     * as long as the target actor is never moved.
     *
     * @param absolute if true, position set with {@link #setPosition(float, float)} (and similar methods) will use
     *            stage coordinates. If false (default), local target's coordinates will be used and position
     *            effectively becomes the offset from the selected actor. */
    public void setAbsolutePosition(final boolean absolute) {
        positionExtractor = absolute ? PositionExtractor.ABSOLUTE : PositionExtractor.RELATIVE;
    }

    /** Executed before drawing. */
    protected void cachePosition() {
        cachedX = getX();
        cachedY = getY();
    }

    /** @param keepWithinStage if true (the default), target's actor position will be validated and kept within stage.
     *            This value has to be set to true if pin is used for a window with
     *            {@link Window#setKeepWithinStage(boolean) keep within stage setting} set to true. */
    public void setKeepWithinStage(final boolean keepWithinStage) {
        this.keepWithinStage = keepWithinStage;
    }

    /** @return if true (the default), target's actor position will be validated and kept within stage. */
    public boolean isKeptWithinStage() {
        return keepWithinStage;
    }

    /** Executed after drawing. Reverts cached position. */
    protected void revertPosition() {
        setPosition(cachedX, cachedY);
    }

    /** Determines how position is interpreted.
     *
     * @author MJ */
    protected static enum PositionExtractor {
        RELATIVE {
            @Override
            public final void beforeDraw(final Pin<?> pin) {
                pin.cachePosition();
                // Setting to 0,0 to get actual actor position. If we'd add pin's x and y now, it could get screwed up
                // with actor's rotation or other settings that we choose to ignore.
                final Actor target = pin.getTarget();
                final Vector2 targetPosition = target.localToStageCoordinates(pin.temp.set(0f, 0f));
                if (pin.isKeptWithinStage()) {
                    targetPosition.x = MathUtils.clamp(targetPosition.x, 0f,
                            pin.getStage().getWidth() - target.getWidth());
                    targetPosition.y = MathUtils.clamp(targetPosition.y, 0f,
                            pin.getStage().getHeight() - target.getHeight());
                }
                pin.setPosition(targetPosition.x + pin.getX(), targetPosition.y + pin.getY());
            }

            @Override
            public void afterDraw(final Pin<?> pin) {
                pin.revertPosition();
            }
        },
        ABSOLUTE;

        /** @param pin will prepare its position for rendering. */
        public void beforeDraw(final Pin<?> pin) {
        }

        /** @param pin will revert its original position (if necessary). */
        public void afterDraw(final Pin<?> pin) {
        }
    }

    /** Updates pins' sizes.
     *
     * @author MJ */
    protected static enum SizeExtractor {
        MIMIC {
            @Override
            public void setSize(final Pin<?> pin) {
                final Actor target = pin.getTarget();
                pin.setSize(target.getWidth(), target.getHeight());
            }
        },
        ABSOLUTE;

        /** @param pin its size will be updated (might be overridden). */
        public void setSize(final Pin<?> pin) {
        }
    }

    @Override
    public void layout() {
        if (content instanceof Layout) {
            ((Layout) content).layout();
        }
    }

    @Override
    public void invalidate() {
        if (content instanceof Layout) {
            ((Layout) content).invalidate();
        }
    }

    /** Pin should not be in a group. */
    @Deprecated
    @Override
    public void invalidateHierarchy() {
        invalidate();
        if (getParent() instanceof Layout) {
            ((Layout) getParent()).invalidate();
        }
    }

    @Override
    public void validate() {
        if (content instanceof Layout) {
            ((Layout) content).validate();
        }
    }

    @Override
    public void pack() {
        if (content instanceof Layout) {
            ((Layout) content).pack();
        }
    }

    @Deprecated
    @Override
    public void setFillParent(final boolean fillParent) {
    }

    @Override
    public void setLayoutEnabled(final boolean enabled) {
        if (content instanceof Layout) {
            ((Layout) content).setLayoutEnabled(enabled);
        }
    }

    @Override
    public float getMinWidth() {
        return getWidth();
    }

    @Override
    public float getMinHeight() {
        return getHeight();
    }

    @Override
    public float getPrefWidth() {
        return getWidth();
    }

    @Override
    public float getPrefHeight() {
        return getHeight();
    }

    @Override
    public float getMaxWidth() {
        return getWidth();
    }

    @Override
    public float getMaxHeight() {
        return getHeight();
    }
}