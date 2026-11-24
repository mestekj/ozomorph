import asyncio
from contextlib import AsyncExitStack

from ozobot import actors
from ozobot.actors.linefollower import data, follow_line, rotate, set_led
from ozobot.evo import EvoHandle
from ozobot.linefollower import Direction, LEDMask, RawColor

# To be inserted
agents = ...
# agents = {0: ((255, 0, 0), ['turnLeft', 'goAhead', 'atStart', 'goAhead', 'atGoal'])}


dispatcher = actors.ActorDispatcher()
actors.set_actor_dispatcher(dispatcher)

async def execute_action(action, color):
    if action == 'goAhead':
        await follow_line(Direction.STRAIGHT)
    elif action == 'turnLeft':
        await rotate(90, 90)
    elif action == 'turnRight':
        await rotate(-90, 90)
    elif action == 'wait':
        await asyncio.sleep(1)
    elif action == 'atStart':
        await set_light_color(*color)
    elif action == 'atGoal':
        await set_front_center_color(*color)
    else:
        raise ValueError(f"Unknown action: {action}")

async def set_light_color(r, g, b):
    await set_led(
        LEDMask.TOP | LEDMask.FRONT_LEFT | LEDMask.FRONT_CENTER | LEDMask.FRONT_RIGHT,
        RawColor(r / 255, g / 255, b / 255),
    )

async def set_front_center_color(r, g, b):
    await set_led(
        LEDMask.TOP | LEDMask.FRONT_LEFT | LEDMask.FRONT_RIGHT,
        RawColor(0, 0, 0),
    )
    await set_led(
        LEDMask.FRONT_CENTER,
        RawColor(r / 255, g / 255, b / 255),
    )

async def main():
    async with AsyncExitStack() as stack:
        for agent in agents:
            device_name = f"Evo_{agent}"
            await stack.enter_async_context(
                dispatcher.connect(device_name, EvoHandle(name=device_name))
            )

        async with asyncio.TaskGroup() as _tg:
            async def _coroutine_follow_plan(agent):
                device_name = f"Evo_{agent}"
                plan = agents[agent][1]
                r, g, b = agents[agent][0]

                with dispatcher.actor(device_name):
                    await set_front_center_color(r, g, b)
                    await data.line_following_speed.write(50)
                    for action in plan:
                        await execute_action(action, (r, g, b))
                    await set_light_color(0, 0, 0)

            tasks = tuple(_tg.create_task(_coroutine_follow_plan(agent)) for agent in agents)
            await asyncio.gather(*tasks)


asyncio.run(main())
