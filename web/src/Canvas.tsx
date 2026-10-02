import { useLayoutEffect, useRef } from 'react';

export type Painter = (context: CanvasRenderingContext2D, width: number, height: number, time: number) => void;

export function Canvas({ draw, label, testId, className = '', animate = false }: {
  draw: Painter; label: string; testId: string; className?: string; animate?: boolean;
}) {
  const ref = useRef<HTMLCanvasElement>(null);
  useLayoutEffect(() => {
    const canvas = ref.current!;
    let width = 1, height = 1, ratio = 1, frame = 0;
    const paint = (time: number) => {
      const context = canvas.getContext('2d')!;
      context.setTransform(ratio, 0, 0, ratio, 0, 0);
      context.fillStyle = '#ffffff';
      context.fillRect(0, 0, width, height);
      draw(context, width, height, time);
    };
    const resize = () => {
      const box = canvas.getBoundingClientRect();
      ratio = Math.min(window.devicePixelRatio || 1, 2);
      width = Math.max(1, box.width);
      height = Math.max(1, box.height);
      canvas.width = Math.round(width * ratio);
      canvas.height = Math.round(height * ratio);
      paint(performance.now());
    };
    resize();
    const observer = new ResizeObserver(resize);
    observer.observe(canvas);
    if (animate) {
      const loop = (time: number) => {
        paint(time);
        frame = requestAnimationFrame(loop);
      };
      frame = requestAnimationFrame(loop);
    }
    return () => {
      observer.disconnect();
      cancelAnimationFrame(frame);
    };
  }, [draw, animate]);
  return <canvas ref={ref} role="img" aria-label={label} data-testid={testId} className={className} />;
}
