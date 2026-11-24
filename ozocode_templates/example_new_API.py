import asyncio
import contextlib
import math
from ozobot.actors.linefollower import data, face_line_direction, follow_line, rotate
from ozobot.linefollower import Direction
from ozobot.evo import Evo, EvoHandle
from ozobot.libblockly.actors import BrowserTerminal
from ozobot import actors

dispatcher = actors.ActorDispatcher()

actors.set_actor_dispatcher(dispatcher)


async def main():
    async with (
        dispatcher.connect("Evo_1", EvoHandle(name="Evo_1")) as evo_1,
        dispatcher.connect("Evo_2", EvoHandle(name="Evo_2")) as evo_2,
        dispatcher.connect("BROWSER_TERMINAL", BrowserTerminal()) as term
    ):
        await data.line_following_speed.write(30)
        with contextlib.suppress(ExceptionGroup):
            async with asyncio.TaskGroup() as _tg:
                async def _coroutine_a():
                    with dispatcher.actor("Evo_1"):
                        await follow_line(Direction.STRAIGHT)
                        await face_line_direction(Direction.LEFT)
                        await follow_line(Direction.STRAIGHT)
                        await rotate(-90, 60)
                        await follow_line(Direction.STRAIGHT)

                async def _coroutine_b():
                    with dispatcher.actor("Evo_2"):
                        await rotate(90, 45)
                        await follow_line(Direction.STRAIGHT)
                        await rotate(180, 45)

                _task_a = _tg.create_task(_coroutine_a())
                _task_b = _tg.create_task(_coroutine_b())
                await asyncio.wait([_task_a, _task_b])


        for _task in [_task_a, _task_b]:
            _task.cancel()
            with contextlib.suppress(asyncio.CancelledError):
                await _task

asyncio.run(main())
