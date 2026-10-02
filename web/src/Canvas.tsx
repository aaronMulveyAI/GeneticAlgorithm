import { useLayoutEffect, useRef } from 'react';

export type Painter = (context: CanvasRenderingContext2D, width: number, height: number) => void;

export function Canvas({ draw, label, testId, className = '' }: {
  draw: Painter; label: string; testId: string; className?: string;
}) {
  const ref = useRef<HTMLCanvasElement>(null);
  useLayoutEffect(() => {
    const canvas = ref.current!;
    const render = () => {
      const box = canvas.getBoundingClientRect();
      const ratio = Math.min(window.devicePixelRatio || 1, 2);
      const width = Math.max(1, box.width);
      const height = Math.max(1, box.height);
      canvas.width = Math.round(width * ratio);
      canvas.height = Math.round(height * ratio);
      const context = canvas.getContext('2d')!;
      context.scale(ratio, ratio);
      context.fillStyle = '#ffffff';
      context.fillRect(0, 0, width, height);
      draw(context, width, height);
    };
    render();
    const observer = new ResizeObserver(render);
    observer.observe(canvas);
    return () => observer.disconnect();
  }, [draw]);
  return <canvas ref={ref} role="img" aria-label={label} data-testid={testId} className={className} />;
}
