/* eslint-disable react-hooks/exhaustive-deps */
import { Animated } from 'react-native';
import { useRef, useMemo, useEffect, useState } from 'react';

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
