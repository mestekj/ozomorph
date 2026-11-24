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

DEPOT_MODE = True
# ozomorph: include actions.py

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
