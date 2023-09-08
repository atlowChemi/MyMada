/* eslint-disable react-hooks/exhaustive-deps */
import { Animated } from 'react-native';
import { makeAutoObservable, observable } from 'mobx';
import { useRef, useMemo, useEffect, useState } from 'react';

export class ColorAnimation {
    private anim: Animated.Value = new Animated.Value(0);
    private _finished = true;
    private prevColor: string;

    constructor(private color: string) {
        this.prevColor = this.color;
        makeAutoObservable<ColorAnimation, 'anim'>(this, {
            anim: observable.ref,
        });
    }

    private onColorChange(color: string) {
        this.prevColor = this.color;
        this.color = color;
        this._finished = true;
    }

    public setColor = (color: string) => {
        if (this.color === color) {
            return;
        }
        this._finished = false;
        Animated.timing(this.anim, {
            toValue: 1,
            duration: 500,
            useNativeDriver: false,
        }).start(() => this.onColorChange(color));
    };

    public get finished() {
        return this._finished;
    }

    public get value() {
        return this.anim.interpolate({
            inputRange: [0, 1],
            outputRange: [this.prevColor, this.color],
        });
    }
}

export const useColorAnimation = (color: string) => {
    const anim = useMemo(() => new Animated.Value(0), [color]);
    const [finished, setFinished] = useState(true);
    const currentColor = useRef(color);
    const nextColor = useMemo(() => color, [color]);

    const animColor = anim.interpolate({
        inputRange: [0, 1],
        outputRange: [currentColor.current, nextColor],
    });

    useEffect(() => {
        setFinished(false);
        Animated.spring(anim, {
            toValue: 1,
            speed: 1,
            useNativeDriver: false,
        }).start(() => {
            currentColor.current = nextColor;
            setFinished(true);
        });
    }, [color]);

    return [animColor, finished] as const;
};

export default useColorAnimation;
