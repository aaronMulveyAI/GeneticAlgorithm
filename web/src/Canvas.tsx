import { useEffect, useLayoutEffect, useRef } from 'react';

export type Painter = (context: CanvasRenderingContext2D, width: number, height: number, time: number) => void;

export function Canvas({ draw, label, testId, className = '', animate = false, onDrag }: {
  draw: Painter; label: string; testId: string; className?: string; animate?: boolean;
  onDrag?: (dx: number, dy: number) => void;
}) {
  const ref = useRef<HTMLCanvasElement>(null);
  const drag = useRef(onDrag);
  drag.current = onDrag;
  const draggable = Boolean(onDrag);
  useEffect(() => {
    if (!draggable) return;
    const canvas = ref.current!;
    let last: { x: number; y: number } | null = null;
    const down = (event: PointerEvent) => {
      last = { x: event.clientX, y: event.clientY };
      canvas.setPointerCapture(event.pointerId);
    };
    const move = (event: PointerEvent) => {
      if (!last) return;
      drag.current?.(event.clientX - last.x, event.clientY - last.y);
      last = { x: event.clientX, y: event.clientY };
    };
    const up = () => { last = null; };
    canvas.addEventListener('pointerdown', down);
    canvas.addEventListener('pointermove', move);
    canvas.addEventListener('pointerup', up);
    canvas.addEventListener('pointercancel', up);
    return () => {
      canvas.removeEventListener('pointerdown', down);
      canvas.removeEventListener('pointermove', move);
      canvas.removeEventListener('pointerup', up);
      canvas.removeEventListener('pointercancel', up);
    };
  }, [draggable]);
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
  return <canvas ref={ref} role="img" aria-label={label} data-testid={testId} className={className}
    style={draggable ? { touchAction: 'pan-y', cursor: 'grab' } : undefined} />;
}
