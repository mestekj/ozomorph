import asyncio
from contextlib import AsyncExitStack

from ozobot import actors
from ozobot.actors.linefollower import data, follow_line, rotate, set_led
from ozobot.evo import EvoHandle
from ozobot.linefollower import Direction, LEDMask, RawColor

# To be inserted
agents = ...
# agents = {0: ((255, 0, 0), ['turnLeft', 'goAhead', 'goAhead']), 1: ((255, 0, 0), ['wait', 'turnLeft', 'goAhead']), 2: ((0, 0, 255), ['wait', 'turnLeft', 'goAhead'])}


dispatcher = actors.ActorDispatcher()
actors.set_actor_dispatcher(dispatcher)

DEPOT_MODE = False
# ozomorph: include actions.py

async def main():
    async with AsyncExitStack() as stack:
        for agent in agents:
            device_name = f"Evo_{agent}"
            await stack.enter_async_context(
                dispatcher.connect(device_name, EvoHandle(name=device_name))
            )

        async with asyncio.TaskGroup() as _tg:
            async def _coroutine_init(agent):
                device_name = f"Evo_{agent}"
                r, g, b = agents[agent][0]
                with dispatcher.actor(device_name):
                    await set_light_color(r, g, b)
                    await data.line_following_speed.write(50)

            async def _execute_action(agent, action):
                device_name = f"Evo_{agent}"
                with dispatcher.actor(device_name):
                    await execute_action(action, agents[agent][0])

            async def _coroutine_finish(agent):
                device_name = f"Evo_{agent}"
                with dispatcher.actor(device_name):
                    await set_light_color(0, 0, 0)

            init_tasks = tuple(_tg.create_task(_coroutine_init(agent)) for agent in agents)
            await asyncio.gather(*init_tasks)

            # Create agents_actions_by_step: list of step_actions where each step_actions is a list of (agent, action) tuples
            max_steps = max(len(agents[agent][1]) for agent in agents) if agents else 0
            agents_actions_by_step = []
            for step in range(max_steps):
                step_actions = [(agent, agents[agent][1][step]) for agent in agents if step < len(agents[agent][1])]
                agents_actions_by_step.append(step_actions)

            # Execute actions step by step, all agents in parallel (in each step).
            for step_actions in agents_actions_by_step:
                tasks = tuple(_tg.create_task(_execute_action(agent, action)) for agent, action in step_actions)
                await asyncio.gather(*tasks)

            finish_tasks = tuple(_tg.create_task(_coroutine_finish(agent)) for agent in agents)
            await asyncio.gather(*finish_tasks)

asyncio.run(main())
